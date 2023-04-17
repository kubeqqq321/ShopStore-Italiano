package com.shopstore.online.serviceImpl;

import com.shopstore.online.JWT.CustomerUsersDetailsService;
import com.shopstore.online.JWT.JwtFilter;
import com.shopstore.online.JWT.JwtUtil;
import com.shopstore.online.constens.CafeConstants;
import com.shopstore.online.dao.UserDao;
import com.shopstore.online.model.User;
import com.shopstore.online.service.UserService;
import com.shopstore.online.utils.CafeUtils;
import com.shopstore.online.utils.EmailUtils;
import com.shopstore.online.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.Authenticator;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    //Signup-------------------------------------------------------------------------------
    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signup {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Pomyślnie Zarejestrowano", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Email juz istnieje", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String,String> requestMap){
      if(requestMap.containsKey("name") && requestMap.containsKey("contactNumber") &&
              requestMap.containsKey("email") && requestMap.containsKey("password")) {
       return true;
      }
    return false;
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }





    //Login-------------------------------------------------------------------------------
    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");

        try{
           //Autoryzacja uzytkownika
            //wyodrębnienie emaila oraz hasła
            Authentication authenticator = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password"))
            );
            //wyodrebnienie roli uzytkownika
            if(authenticator.isAuthenticated()){
               if (customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")){
                   //generujemy token oraz wydrebniamy email oraz role z userDetail
                   return new ResponseEntity<String>("{\"token\":\""+jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(),
                           customerUsersDetailsService.getUserDetail().getRole())+"\"}", HttpStatus.OK);
               }
               else {
                   return new ResponseEntity<String>("{\"message\":\""+"Zaczekaj na potwierzenie przez Admina"+"\"}", HttpStatus.BAD_REQUEST);
               }
            }

        }catch(Exception ex){
            log.error("{}",ex);
        }
        return new ResponseEntity<String>("{\"message\":\""+"Coś nie tak"+"\"}", HttpStatus.BAD_REQUEST);
    }


    //Get All Users --------------------------------------------------------------------------------------------------------------
    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try{
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userDao.getAllUser(),HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }


    //Update --------------------------------------------------------------------------------------------------------------
    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                //sprawdzamy czy uzytownik o podanym id w ogole istnieje
              Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
              //wysyłamy emil do wszytskich adminów
              sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(),userDao.getAllAdmin());

              if(!optional.isEmpty()){
                  userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                  return CafeUtils.getResponseEntity("Status użytkownika pomyślnie zaktualizowany", HttpStatus.OK);
              }
              else {
                  CafeUtils.getResponseEntity("Uzytkownik o podanym id nie istnieje",HttpStatus.OK);
              }
            }
            else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {

        allAdmin.remove(jwtFilter.getCurrentUser());
        if(status != null && status.equalsIgnoreCase("true")){
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Konto Potwierdzone", "USER:- " + user +"\n został zatwierdzony przez: \nADMIN: "+ jwtFilter.getCurrentUser(),allAdmin);
        }
        else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Konto Zablokowane", "USER:- " + user +"\n został zablokowany przez: \nADMIN: "+ jwtFilter.getCurrentUser(),allAdmin);

        }

    }

}
