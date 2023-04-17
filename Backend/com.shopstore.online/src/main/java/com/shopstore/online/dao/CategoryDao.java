package com.shopstore.online.dao;

import com.shopstore.online.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category,Integer> {

    List<Category> getAllCategory();



}
