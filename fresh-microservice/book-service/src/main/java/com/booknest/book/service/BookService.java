package com.booknest.book.service;

import com.booknest.book.dto.BookResponse;
import com.booknest.book.dto.BookUpsertRequest;

import java.util.List;

public interface BookService {
    BookResponse addBook(BookUpsertRequest request);
    List<BookResponse> getAllBooks();
    BookResponse getBookById(int id);
    List<BookResponse> searchBooks(String keyword);
    List<BookResponse> getByGenre(String genre);
    BookResponse updateBook(int id, BookUpsertRequest request);
    void deleteBook(int id);
    void updateStock(int id, int quantity);
}