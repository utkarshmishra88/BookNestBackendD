package com.booknest.book.repository;

import com.booknest.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    Optional<Book> findByIsbn(String isbn);

    List<Book> findByCategory_CategoryNameIgnoreCase(String categoryName);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchByKeyword(@Param("keyword") String keyword);
}