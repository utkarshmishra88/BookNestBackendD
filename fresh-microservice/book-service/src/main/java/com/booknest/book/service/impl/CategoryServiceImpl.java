package com.booknest.book.service.impl;

import com.booknest.book.dto.CategoryResponse;
import com.booknest.book.dto.CategoryUpsertRequest;
import com.booknest.book.entity.Category;
import com.booknest.book.repository.CategoryRepository;
import com.booknest.book.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryUpsertRequest request) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name already exists");
        }

        Category saved = categoryRepository.save(Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build());

        return toResponse(saved);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(int id, CategoryUpsertRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.findByCategoryNameIgnoreCase(request.getCategoryName()).ifPresent(other -> {
            if (!other.getCategoryId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name already exists");
            }
        });

        existing.setCategoryName(request.getCategoryName());
        existing.setDescription(request.getDescription());

        return toResponse(categoryRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteCategory(int id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .categoryId(c.getCategoryId())
                .categoryName(c.getCategoryName())
                .description(c.getDescription())
                .build();
    }
}