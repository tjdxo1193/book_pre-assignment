package book.repository;

import book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("select b from Book b join b.bookCategories bc where bc.id = :id")
    List<Book> findAllByCategoryId(@Param(value = "id") Long categoryId);

    // 정확한 제목, 정확한 지은이가 아닌 글자로 검색하는 ex) "가" 로 검색한다면 가지, 가마 이렇게 나오는 걸 요구한다면 %% 사용해서 쿼리를 바꿀 필요 있음.
    @Query("select b from Book b where b.author = :author and b.title = :title")
    List<Book> findAllByAuthorAndTitle(@Param(value = "author") String author, @Param(value = "title") String title);

}
