package com.booknest.book.service.impl;

import com.booknest.book.dto.CategoryResponse;
import com.booknest.book.dto.CategoryUpsertRequest;
import com.booknest.book.entity.Category;
import com.booknest.book.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category validCategory;
    private CategoryUpsertRequest validRequest;

    @BeforeEach
    void setUp() {
        validCategory = Category.builder()
                .categoryId(1)
                .categoryName("Fiction")
                .description("Fiction books")
                .build();

        validRequest = new CategoryUpsertRequest();
        validRequest.setCategoryName("Fiction");
        validRequest.setDescription("Fiction books");
    }

    @Test
    void testAddCategory_Success() {
        when(categoryRepository.existsByCategoryNameIgnoreCase("Fiction")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);

        CategoryResponse response = categoryService.addCategory(validRequest);

        assertEquals(1, response.getCategoryId());
        assertEquals("Fiction", response.getCategoryName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testAddCategory_AlreadyExists() {
        when(categoryRepository.existsByCategoryNameIgnoreCase("Fiction")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> categoryService.addCategory(validRequest));
    }

    @Test
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(validCategory));

        List<CategoryResponse> list = categoryService.getAllCategories();

        assertEquals(1, list.size());
    }

    @Test
    void testUpdateCategory_Success() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(validCategory));
        when(categoryRepository.findByCategoryNameIgnoreCase("Fiction")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);

        CategoryResponse response = categoryService.updateCategory(1, validRequest);

        assertEquals("Fiction", response.getCategoryName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.existsById(1)).thenReturn(true);
        categoryService.deleteCategory(1);
        verify(categoryRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteCategory_NotFound() {
        when(categoryRepository.existsById(1)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> categoryService.deleteCategory(1));
    }
}
