package book.service;
import book.api.BookApiController;
import book.dto.RequestBookDto;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
@ExtendWith(MockitoExtension.class)
class BookServiceUnitTest {
    @InjectMocks
    private BookService bookService;
    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookCategoryRepository bookCategoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    static final String TITLE = "Title";
    static final String AUTHOR = "Author";

    @Test
    @DisplayName("카테고리별 도서 검색")
    void getBooksByCategory() {
        // Arrange
        Category category =
                Category.builder()
                        .id(1L)
                        .name("인문학")
                        .build();
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        List<BookCategory> categories = new ArrayList<>();
        List<Book> expectedBooks =
                Arrays.asList(
                        new Book(1L, "Title1", "황성태", BookStatus.AVAILABLE, categories),
                        new Book(2L, "Title2", "황성태", BookStatus.UNAVAILABLE, categories));
        when(bookRepository.findAllByCategory(any())).thenReturn(expectedBooks);

        // Act
        List<ResponseBookDto> actualBooks = bookService.getBooksByCategory(1L);

        // Assert
        assertNotNull(actualBooks);
        assertEquals(expectedBooks.size(), actualBooks.size());
    }

    @Test
    @DisplayName("지은이 및 제목별 도서 검색")
    void getBooksByAuthorAndTitle() {
        // given
        List<Book> bookList = new ArrayList<>();
        Book book = Book.builder()
                .id(1L)
                .title(TITLE)
                .author(AUTHOR)
                .build();
        bookList.add(book);

        RequestBookDto requestDto_one = new RequestBookDto(TITLE, AUTHOR);
        given(bookRepository.findAllByAuthorAndTitle(anyString(), anyString())).willReturn(bookList);

        // when
        List<ResponseBookDto> actualBooks_one = bookService.getBooksByAuthorAndTitle(requestDto_one);

        // than
        assertThat(actualBooks_one.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("도서 대여 중단 or 대여 활성화 테스트")
    void changeBookStatus() {
        // Arrange
        List<BookCategory> categories = new ArrayList<>();
        Book testBook = new Book(1L, "Title", "Author", BookStatus.AVAILABLE, categories);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));

        // Act
        assertDoesNotThrow(() -> bookService.changeBookStatus(1L, BookStatus.UNAVAILABLE));

        // Assert
        assertEquals(BookStatus.UNAVAILABLE, testBook.getStatus());
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("도서 등록 성공 테스트")
    void registerNewBook() {
        // Arrange
        BookApiController.CreateBookCommand createCommand = new BookApiController.CreateBookCommand("New Book", "New Author", Arrays.asList(1L, 2L));

        Category category1 = new Category(1L,"Category1", new ArrayList<BookCategory>());
        Category category2 = new Category(1L, "Category2", new ArrayList<BookCategory>());
        when(categoryRepository.findAllById(any())).thenReturn(Arrays.asList(category1, category2));

        Book savedBook = new Book(1L, "New Book", "New Author", BookStatus.AVAILABLE, new ArrayList<>());
        when(bookRepository.save(any())).thenReturn(savedBook);

        // Act
        ResponseBookDto responseBookDto = bookService.registerNewBook(createCommand);

        // Assert
        assertNotNull(responseBookDto);
        assertEquals(savedBook.getId(), responseBookDto.getBookId());
        verify(bookCategoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("카테고리 변경 성공 테스트")
    void changeCategories() {
        // Arrange
        Book testBook = new Book(1L,"Test Title", "Test Author", BookStatus.AVAILABLE, new ArrayList<>());
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));

        List<Category> afterCategoryList = Arrays.asList(new Category(1L, "Category1", new ArrayList<>()), new Category(2L,"Category2", new ArrayList<>()));
        when(categoryRepository.findAllById(anyList())).thenReturn(afterCategoryList);

        List<BookCategory> beforeCategoryList = Arrays.asList(
                new BookCategory(1L ,testBook, new Category(1L, "Category1", new ArrayList<>())),
                new BookCategory(1L ,testBook, new Category(2L, "Category2", new ArrayList<>()))
        );
        when(bookCategoryRepository.findAllByBook(any())).thenReturn(beforeCategoryList);

        // Act
        assertDoesNotThrow(() -> bookService.changeCategories(1L, new BookApiController.UpdateBookCategoryCommand(
                List.of(1L)
        )));

        // Assert
        verify(bookCategoryRepository, times(1)).deleteAll(anyList());
        verify(bookCategoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("카테고리 변경 예외 테스트 - 이전 카테고리가 0개 일때")
    void changeCategoriesWithEmptyBookCategoryList() {
        // Arrange
        Book testBook = new Book(1L, "Title", "Author", BookStatus.AVAILABLE, new ArrayList<>());
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));

        // Act and Assert
        BookCategoryNotFoundException exception = assertThrows(BookCategoryNotFoundException.class,
                () -> bookService.changeCategories(1L, new BookApiController.UpdateBookCategoryCommand(
                        Arrays.asList(1L, 2L)
                )));

        assertEquals("book category is Empty. Category Not Found", exception.getMessage());
        verify(bookCategoryRepository, never()).deleteAll(anyList());
        verify(bookCategoryRepository, never()).saveAll(anyList());
    }
}