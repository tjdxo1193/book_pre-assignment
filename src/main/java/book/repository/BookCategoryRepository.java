package book.repository;

import book.entity.Book;
import book.entity.BookCategory;
import book.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    @Query("select bc from BookCategory bc where bc.book = :book")
    List<BookCategory> findAllByBook(@Param(value = "book") Book book);

    List<BookCategory> findAllByBookIdAndCategoryNotIn(Long book_id, Collection<Category> category);

}
