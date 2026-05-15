package com.booknest.book.resource;

import com.booknest.book.dto.BookResponse;
import com.booknest.book.dto.BookUpsertRequest;
import com.booknest.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookResource {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return new ResponseEntity<>(bookService.getAllBooks(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable int id) {
        return new ResponseEntity<>(bookService.getBookById(id), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "title", required = false) String title
    ) {
        String key = (keyword != null && !keyword.isBlank()) ? keyword : title;
        return new ResponseEntity<>(bookService.searchBooks(key), HttpStatus.OK);
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<BookResponse>> getByGenre(@PathVariable String genre) {
        return new ResponseEntity<>(bookService.getByGenre(genre), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookUpsertRequest request) {
        return new ResponseEntity<>(bookService.addBook(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable int id, @Valid @RequestBody BookUpsertRequest request) {
        return new ResponseEntity<>(bookService.updateBook(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBook(@PathVariable int id) {
        bookService.deleteBook(id);
        return new ResponseEntity<>("Book successfully deleted", HttpStatus.OK);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<String> updateStock(@PathVariable int id, @RequestParam int quantity) {
        bookService.updateStock(id, quantity);
        return new ResponseEntity<>("Stock adjusted successfully", HttpStatus.OK);
    }
}