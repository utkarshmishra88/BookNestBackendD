package com.booknest.book.service.impl;

import com.booknest.book.dto.BookResponse;
import com.booknest.book.dto.BookUpsertRequest;
import com.booknest.book.entity.Book;
import com.booknest.book.entity.Category;
import com.booknest.book.repository.BookRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book validBook;
    private Category validCategory;
    private BookUpsertRequest validRequest;

    @BeforeEach
    void setUp() {
        validCategory = Category.builder()
                .categoryId(1)
                .categoryName("Fiction")
                .build();

        validBook = Book.builder()
                .bookId(100)
                .title("Test Book")
                .author("Test Author")
                .isbn("123456789")
                .category(validCategory)
                .price(19.99)
                .stock(10)
                .build();

        validRequest = new BookUpsertRequest();
        validRequest.setTitle("Test Book");
        validRequest.setAuthor("Test Author");
        validRequest.setIsbn("123456789");
        validRequest.setCategoryId(1);
        validRequest.setPrice(19.99);
        validRequest.setStock(10);
    }

    @Test
    void testAddBook_Success() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(1)).thenReturn(Optional.of(validCategory));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        BookResponse response = bookService.addBook(validRequest);

        assertEquals(100, response.getBookId());
        assertEquals("Test Book", response.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testAddBook_IsbnExists() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(validBook));

        assertThrows(ResponseStatusException.class, () -> bookService.addBook(validRequest));
    }

    @Test
    void testGetAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(validBook));
        List<BookResponse> books = bookService.getAllBooks();
        assertEquals(1, books.size());
    }

    @Test
    void testGetBookById_Success() {
        when(bookRepository.findById(100)).thenReturn(Optional.of(validBook));
        BookResponse response = bookService.getBookById(100);
        assertEquals("Test Book", response.getTitle());
    }

    @Test
    void testGetBookById_NotFound() {
        when(bookRepository.findById(100)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> bookService.getBookById(100));
    }

    @Test
    void testSearchBooks() {
        when(bookRepository.searchByKeyword("Test")).thenReturn(List.of(validBook));
        List<BookResponse> books = bookService.searchBooks("Test");
        assertEquals(1, books.size());
    }

    @Test
    void testUpdateBook_Success() {
        when(bookRepository.findById(100)).thenReturn(Optional.of(validBook));
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findById(1)).thenReturn(Optional.of(validCategory));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        validRequest.setTitle("Updated Title");
        BookResponse response = bookService.updateBook(100, validRequest);

        assertEquals(100, response.getBookId());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testDeleteBook_Success() {
        when(bookRepository.existsById(100)).thenReturn(true);
        bookService.deleteBook(100);
        verify(bookRepository, times(1)).deleteById(100);
    }

    @Test
    void testUpdateStock_Success() {
        when(bookRepository.findById(100)).thenReturn(Optional.of(validBook));
        bookService.updateStock(100, -5);
        assertEquals(5, validBook.getStock());
        verify(bookRepository, times(1)).save(validBook);
    }

    @Test
    void testUpdateStock_InsufficientStock() {
        when(bookRepository.findById(100)).thenReturn(Optional.of(validBook));
        assertThrows(ResponseStatusException.class, () -> bookService.updateStock(100, -15));
    }
}
