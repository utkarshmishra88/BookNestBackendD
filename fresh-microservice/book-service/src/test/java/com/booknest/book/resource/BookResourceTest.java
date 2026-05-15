package com.booknest.book.resource;

import com.booknest.book.dto.BookResponse;
import com.booknest.book.dto.BookUpsertRequest;
import com.booknest.book.service.BookService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class BookResourceTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookResource bookResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllBooks_ShouldReturnList() {
        when(bookService.getAllBooks()).thenReturn(Collections.singletonList(new BookResponse()));
        ResponseEntity<List<BookResponse>> result = bookResource.getAllBooks();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getBookById_ShouldReturnBook() {
        BookResponse response = new BookResponse();
        when(bookService.getBookById(1)).thenReturn(response);
        ResponseEntity<BookResponse> result = bookResource.getBookById(1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void searchBooks_ShouldReturnList() {
        when(bookService.searchBooks(anyString())).thenReturn(Collections.singletonList(new BookResponse()));
        ResponseEntity<List<BookResponse>> result = bookResource.searchBooks("keyword", null);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getByGenre_ShouldReturnList() {
        when(bookService.getByGenre(anyString())).thenReturn(Collections.singletonList(new BookResponse()));
        ResponseEntity<List<BookResponse>> result = bookResource.getByGenre("Fiction");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void addBook_ShouldReturnCreated() {
        BookUpsertRequest request = new BookUpsertRequest();
        BookResponse response = new BookResponse();
        when(bookService.addBook(any())).thenReturn(response);
        ResponseEntity<BookResponse> result = bookResource.addBook(request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateBook_ShouldReturnOk() {
        BookUpsertRequest request = new BookUpsertRequest();
        BookResponse response = new BookResponse();
        when(bookService.updateBook(anyInt(), any())).thenReturn(response);
        ResponseEntity<BookResponse> result = bookResource.updateBook(1, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteBook_ShouldReturnOk() {
        doNothing().when(bookService).deleteBook(anyInt());
        ResponseEntity<String> result = bookResource.deleteBook(1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Book successfully deleted", result.getBody());
    }

    @Test
    void updateStock_ShouldReturnOk() {
        doNothing().when(bookService).updateStock(anyInt(), anyInt());
        ResponseEntity<String> result = bookResource.updateStock(1, 10);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Stock adjusted successfully", result.getBody());
    }
}
