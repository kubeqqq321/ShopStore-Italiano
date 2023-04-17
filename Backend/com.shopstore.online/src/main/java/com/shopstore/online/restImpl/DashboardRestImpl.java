package com.shopstore.online.restImpl;

import com.shopstore.online.rest.DashboardRest;
import com.shopstore.online.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardRestImpl implements DashboardRest {

    @Autowired
    DashboardService dashboardService;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        return dashboardService.getDetails();
    }
}
