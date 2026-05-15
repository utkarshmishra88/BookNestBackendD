package com.booknest.book.service.impl;

import com.booknest.book.dto.BookResponse;
import com.booknest.book.dto.BookUpsertRequest;
import com.booknest.book.dto.CategoryResponse;
import com.booknest.book.entity.Book;
import com.booknest.book.entity.Category;
import com.booknest.book.repository.BookRepository;
import com.booknest.book.repository.CategoryRepository;
import com.booknest.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BookResponse addBook(BookUpsertRequest request) {
        if (request.getIsbn() != null && !request.getIsbn().isBlank() && bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID"));

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .category(category)
                .publisher(request.getPublisher())
                .price(request.getPrice())
                .stock(request.getStock())
                .rating(request.getRating())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .publishedDate(request.getPublishedDate())
                .build();

        return toResponse(bookRepository.save(book));
    }

    @Override
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public BookResponse getBookById(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        return toResponse(book);
    }

    @Override
    public List<BookResponse> searchBooks(String keyword) {
        String key = (keyword == null) ? "" : keyword.trim();
        if (key.isEmpty()) return getAllBooks();
        return bookRepository.searchByKeyword(key).stream().map(this::toResponse).toList();
    }

    @Override
    public List<BookResponse> getByGenre(String genre) {
        return bookRepository.findByCategory_CategoryNameIgnoreCase(genre).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public BookResponse updateBook(int id, BookUpsertRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
            bookRepository.findByIsbn(request.getIsbn()).ifPresent(other -> {
                if (!other.getBookId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN already exists");
                }
            });
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID"));

        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setIsbn(request.getIsbn());
        existing.setCategory(category);
        existing.setPublisher(request.getPublisher());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());
        existing.setRating(request.getRating());
        existing.setDescription(request.getDescription());
        existing.setCoverImageUrl(request.getCoverImageUrl());
        existing.setPublishedDate(request.getPublishedDate());

        return toResponse(bookRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteBook(int id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStock(int id, int quantity) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        int next = book.getStock() + quantity;
        if (next < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
        }

        book.setStock(next);
        bookRepository.save(book);
    }

    private BookResponse toResponse(Book b) {
        return BookResponse.builder()
                .bookId(b.getBookId())
                .title(b.getTitle())
                .author(b.getAuthor())
                .isbn(b.getIsbn())
                .category(CategoryResponse.builder()
                        .categoryId(b.getCategory().getCategoryId())
                        .categoryName(b.getCategory().getCategoryName())
                        .description(b.getCategory().getDescription())
                        .build())
                .publisher(b.getPublisher())
                .price(b.getPrice())
                .stock(b.getStock())
                .rating(b.getRating())
                .description(b.getDescription())
                .coverImageUrl(b.getCoverImageUrl())
                .publishedDate(b.getPublishedDate())
                .build();
    }
}