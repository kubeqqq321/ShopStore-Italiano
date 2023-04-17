import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import jwt_Decode from 'jwt-decode';
import { GlobalConstants } from '../shared/global-constants';
import { AuthService } from './auth.service';
import { SnackbarService } from './snackbar.service';

@Injectable({
  providedIn: 'root'
})
export class RouteGuardService {

  constructor(public auth:AuthService,public router:Router,private snackbarService:SnackbarService) { }

  canActivate(route:ActivatedRouteSnapshot):boolean{
    let expectedRoleArray = route.data;
    expectedRoleArray = expectedRoleArray.expectedRole;

    const token:any = localStorage.getItem('token');

    var tokenDecode:any

    try{
      tokenDecode = jwt_Decode(token);
    }catch(err){
      localStorage.clear();
      this.router.navigate(['/'])
    }

    let expectedRole = '';

    for(let i =0; i < expectedRoleArray.length; i++){
      if(expectedRoleArray[i] == tokenDecode.role){
        expectedRole = tokenDecode.role;
      }
    }

    if(tokenDecode.role =='user' || tokenDecode.role =='admin'){
      if(this.auth.isAuthenticated() && tokenDecode.role == expectedRole){
        return true;
      }
      this.snackbarService.openSnackBar(GlobalConstants.unauthorized,GlobalConstants.error);
      this.router.navigate(['/cafe/dashboard']);
      return false;
    }
    else{
      this.router.navigate(['/']);
      localStorage.clear;
      return false;
    }

  }

}
