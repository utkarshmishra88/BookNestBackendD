package com.booknest.book.resource;

import com.booknest.book.dto.CategoryResponse;
import com.booknest.book.dto.CategoryUpsertRequest;
import com.booknest.book.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryResource {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return new ResponseEntity<>(categoryService.getAllCategories(), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> addCategory(@Valid @RequestBody CategoryUpsertRequest request) {
        return new ResponseEntity<>(categoryService.addCategory(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable int id, @Valid @RequestBody CategoryUpsertRequest request) {
        return new ResponseEntity<>(categoryService.updateCategory(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>("Category successfully deleted", HttpStatus.OK);
    }
}