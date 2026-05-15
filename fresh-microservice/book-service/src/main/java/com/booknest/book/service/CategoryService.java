package com.booknest.book.service;

import com.booknest.book.dto.CategoryResponse;
import com.booknest.book.dto.CategoryUpsertRequest;

import java.util.List;

public interface CategoryService {
    CategoryResponse addCategory(CategoryUpsertRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse updateCategory(int id, CategoryUpsertRequest request);
    void deleteCategory(int id);
}