package com.shopstore.online.serviceImpl;

import com.shopstore.online.dao.BillDao;
import com.shopstore.online.dao.CategoryDao;
import com.shopstore.online.dao.ProductDao;
import com.shopstore.online.model.Product;
import com.shopstore.online.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    BillDao billDao;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        Map<String,Object> map = new HashMap<>();
        map.put("Kategorie", categoryDao.count());
        map.put("Produkty" , productDao.count());
        map.put("Rachunki" , billDao.count());
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
