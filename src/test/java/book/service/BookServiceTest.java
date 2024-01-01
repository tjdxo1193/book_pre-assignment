package book.service;
import book.api.BookApiController;
import book.dto.ResponseBookDto;
import book.entity.Book;
import book.entity.BookCategory;
import book.entity.Category;
import book.enums.BookStatus;
import book.exception.BookCategoryNotFoundException;
import book.repository.BookCategoryRepository;
import book.repository.BookRepository;
import book.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@DataJpaTest
class BookServiceTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, categoryRepository, bookCategoryRepository);
    }

    @Test
    @DisplayName("도서 대여 중단 or 대여 활성화 테스트")
    void changeBookStatus() {
        // Given
        Category category = Category
                .builder()
                .name("Test Category")
                .build();
        categoryRepository.save(category);

        Book book = Book.builder()
                .title("Test Title")
                .author("Test Author")
                .status(BookStatus.AVAILABLE)
                .build();
        bookRepository.save(book);

        BookCategory bookCategory = BookCategory.builder()
                .book(book)
                .category(category)
                .build();
        bookCategoryRepository.save(bookCategory);

        // assert
        assertDoesNotThrow(() -> book.updateStatus(BookStatus.UNAVAILABLE));
        assertEquals(BookStatus.UNAVAILABLE, book.getStatus());
    }

    @Test
    @DisplayName("도서 등록 성공 테스트")
    void registerNewBook() {
        // Given
        List<Category> category = List.of(
                    Category
                        .builder()
                        .name("인문학")
                        .build(),
                    Category
                        .builder()
                        .name("사회학")
                        .build()
                    );
        categoryRepository.saveAll(category);
        BookApiController.CreateBookCommand createCommand =
                new BookApiController.CreateBookCommand("도서", "황성태",
                        category.stream().map(Category::getId).toList());

        ResponseBookDto bookDto = bookService.registerNewBook(createCommand);

        Book book = bookRepository.findById(bookDto.getBookId()).orElseGet(null);
        List<BookCategory> bookCategory = bookCategoryRepository.findAllByBook(book);

        assertEquals(book.getTitle(), createCommand.getTitle());
        assertEquals(book.getAuthor(), createCommand.getAuthor());
        assertEquals(bookCategory.size(), bookDto.getCategories().size());
    }

    @Transactional
    @Test
    @DisplayName("카테고리 변경 성공 테스트")
    void changeCategories() {
        // Given
        List<Category> categories = List.of(
                Category
                        .builder()
                        .name("인문학")
                        .build(),
                Category
                        .builder()
                        .name("사회학")
                        .build(),
                Category
                        .builder()
                        .name("IT")
                        .build()
        );
        categoryRepository.saveAll(categories);

        Book book = Book.builder()
                .title("Test Title")
                .author("Test Author")
                .status(BookStatus.AVAILABLE)
                .build();
        bookRepository.save(book);

        List<BookCategory> preBookCategoryList =
                List.of(
                        BookCategory.builder()
                                .book(book)
                                .category(categories.get(0))
                                .build(),
                        BookCategory.builder()
                                .book(book)
                                .category(categories.get(1))
                                .build()
                );

        bookCategoryRepository.saveAll(preBookCategoryList);

        BookApiController.UpdateBookCategoryCommand command =
                new BookApiController.UpdateBookCategoryCommand(
                        List.of(categories.get(1).getId(), categories.get(2).getId())
                );

        //when
        bookService.changeCategories(book.getId(), command);
        List<BookCategory> afterBookCategories = bookCategoryRepository.findAllByBook(book);

        //than
        assertNotEquals(preBookCategoryList, afterBookCategories);
        assertEquals(command.getCategoryIds()
                , afterBookCategories.stream().map(BookCategory::getCategory).map(Category::getId).toList());
    }

    @Test
    @DisplayName("카테고리 변경 예외 테스트 - 이전 카테고리가 0개 일때")
    void changeCategoriesWithEmptyBookCategoryList() {
        // Given
        List<Category> categories = List.of(
                Category
                        .builder()
                        .name("인문학")
                        .build(),
                Category
                        .builder()
                        .name("사회학")
                        .build(),
                Category
                        .builder()
                        .name("IT")
                        .build()
        );
        categoryRepository.saveAll(categories);

        Book book = Book.builder()
                .title("Test Title")
                .author("Test Author")
                .status(BookStatus.AVAILABLE)
                .build();
        bookRepository.save(book);

        BookApiController.UpdateBookCategoryCommand command =
                new BookApiController.UpdateBookCategoryCommand(
                        List.of(2L, 3L)
                );

        //throws
        assertThrows(BookCategoryNotFoundException.class, () -> {
            bookService.changeCategories(book.getId(), command);
        });
    }
}