package book.repository;

import book.entity.Book;
import book.entity.BookCategory;
import book.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    // BookService 의 조회 메서드들은 단순 쿼리 테스트로 충분히 기능을 테스트 할 수 있다고 생각한다.
    // 그래서 조회 기능 테스트는 여기서하고 데이터 변경 작업만 BookServiceTest 에서 단위 테스트를 한다.
    @Test
    @DisplayName("지은이 및 제목별 도서 검색 쿼리 테스트")
    void testGetBooksByAuthorAndTitle() {
        // Given
        Category category = Category
                .builder()
                .name("Test Category")
                .build();
        categoryRepository.save(category);

        Book book = Book.builder()
                .title("Test Title")
                .author("Test Author")
                .build();
        bookRepository.save(book);

        BookCategory bookCategory = BookCategory.builder()
                .book(book)
                .category(category)
                .build();
        bookCategoryRepository.save(bookCategory);

        // When
        List<Book> books = bookRepository.findAllByAuthorAndTitle("Test Author", "Test Title");

        // Then
        assertEquals(1, books.size());
        assertEquals("Test Title", books.get(0).getTitle());
        assertEquals("Test Author", books.get(0).getAuthor());
    }

    @Test
    @DisplayName("카테고리별 도서 검색 쿼리 테스트")
    void getBooksByCategory() {
        // Given
        Category category = Category
                .builder()
                .name("인문학")
                .build();
        categoryRepository.save(category);
        List<Book> bookList = List.of(
                Book.builder()
                        .title("도서1")
                        .author("황성태")
                        .build(),
                Book.builder()
                        .title("도서2")
                        .author("황성태")
                        .build()
        );

        bookRepository.saveAll(bookList);
        List<BookCategory> bookCategoryList =
                List.of(
                    BookCategory.builder()
                        .book(bookList.get(0))
                        .category(category)
                        .build(),
                    BookCategory.builder()
                        .book(bookList.get(1))
                        .category(category)
                        .build()
                    );

        bookCategoryRepository.saveAll(bookCategoryList);
        // When
        List<Book> books = bookRepository.findAllByCategory(category);

        // Then
        assertEquals(2, books.size());
        assertEquals("도서1", books.get(0).getTitle());
        assertEquals("도서2", books.get(1).getTitle());
    }
}