package book.repository;

import book.entity.Book;
import book.entity.BookCategory;
import book.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    List<BookCategory> findAllByBookId(Long bookId);

    @Query("select bc from BookCategory bc where bc.book = :book and bc.category not in :categories")
    List<BookCategory> findAllByBookIdAndNotInCategoryIds(@Param(value = "book") Book book, @Param(value= "categories") List<Category> categories);

}
