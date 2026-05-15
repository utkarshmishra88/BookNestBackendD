package com.booknest.book.repository;

import com.booknest.book.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}