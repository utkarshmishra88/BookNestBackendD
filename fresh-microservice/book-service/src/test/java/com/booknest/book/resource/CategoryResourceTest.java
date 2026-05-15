package com.booknest.book.resource;

import com.booknest.book.dto.CategoryResponse;
import com.booknest.book.dto.CategoryUpsertRequest;
import com.booknest.book.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class CategoryResourceTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryResource categoryResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        when(categoryService.getAllCategories()).thenReturn(Collections.singletonList(new CategoryResponse()));
        ResponseEntity<List<CategoryResponse>> result = categoryResource.getAllCategories();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void addCategory_ShouldReturnCreated() {
        CategoryUpsertRequest request = new CategoryUpsertRequest();
        CategoryResponse response = new CategoryResponse();
        when(categoryService.addCategory(any())).thenReturn(response);
        ResponseEntity<CategoryResponse> result = categoryResource.addCategory(request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateCategory_ShouldReturnOk() {
        CategoryUpsertRequest request = new CategoryUpsertRequest();
        CategoryResponse response = new CategoryResponse();
        when(categoryService.updateCategory(anyInt(), any())).thenReturn(response);
        ResponseEntity<CategoryResponse> result = categoryResource.updateCategory(1, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteCategory_ShouldReturnOk() {
        doNothing().when(categoryService).deleteCategory(anyInt());
        ResponseEntity<String> result = categoryResource.deleteCategory(1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Category successfully deleted", result.getBody());
    }
}
