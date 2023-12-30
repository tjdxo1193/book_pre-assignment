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
import book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
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

    @Test
    void getBooksByCategory() {
        // Arrange
        Category category =
                Category.builder()
                        .id(1L)
                        .name("인문학")
                        .build();
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        List<Book> expectedBooks =
                Arrays.asList(
                        new Book(1L, "Title1", "황성태", BookStatus.AVAILABLE),
                        new Book(2L, "Title2", "황성태", BookStatus.UNAVAILABLE));
        when(bookRepository.findAllByCategory(any())).thenReturn(expectedBooks);

        // Act
        List<ResponseBookDto> actualBooks = bookService.getBooksByCategory(1L);

        // Assert
        assertNotNull(actualBooks);
        assertEquals(expectedBooks.size(), actualBooks.size());
    }

    @Test
    void getBooksByAuthorAndTitle() {
        // Arrange
        RequestBookDto requestDto = new RequestBookDto("Author", "Title");

        List<Book> expectedBooks = Arrays.asList(new Book("Author", "Title", BookStatus.AVAILABLE));
        when(bookRepository.findAllByAuthorAndTitle(anyString(), anyString())).thenReturn(expectedBooks);

        // Act
        List<ResponseBookDto> actualBooks = bookService.getBooksByAuthorAndTitle(requestDto);

        // Assert
        assertNotNull(actualBooks);
        assertEquals(expectedBooks.size(), actualBooks.size());
    }

    @Test
    void changeBookStatus() {
        // Arrange
        Book testBook = new Book("Test Author", "Test Title", BookStatus.AVAILABLE);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));

        // Act
        assertDoesNotThrow(() -> bookService.changeBookStatus(1L, BookStatus.UNAVAILABLE));

        // Assert
        assertEquals(BookStatus.UNAVAILABLE, testBook.getStatus());
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    void registerNewBook() {
        // Arrange
        BookApiController.CreateBookCommand createCommand = new BookApiController.CreateBookCommand();
        createCommand.setTitle("New Book");
        createCommand.setAuthor("New Author");
        createCommand.setCategoryIds(Arrays.asList(1L, 2L));

        Category category1 = new Category("Category1");
        Category category2 = new Category("Category2");
        when(categoryRepository.findAllById(any())).thenReturn(Arrays.asList(category1, category2));

        Book savedBook = new Book("New Author", "New Book", BookStatus.AVAILABLE);
        when(bookRepository.save(any())).thenReturn(savedBook);

        // Act
        ResponseBookDto responseBookDto = bookService.registerNewBook(createCommand);

        // Assert
        assertNotNull(responseBookDto);
        assertEquals(savedBook.getId(), responseBookDto.getBookId());
        verify(bookCategoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void changeCategories() {
        // Arrange
        Book testBook = new Book("Test Author", "Test Title", BookStatus.AVAILABLE);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));

        List<Category> afterCategoryList = Arrays.asList(new Category("Category1"), new Category("Category2"));
        when(categoryRepository.findAllById(anyList())).thenReturn(afterCategoryList);

        List<BookCategory> beforeCategoryList = Arrays.asList(
                new BookCategory(testBook, new Category("Category1")),
                new BookCategory(testBook, new Category("Category2"))
        );
        when(bookCategoryRepository.findAllByBook(any())).thenReturn(beforeCategoryList);

        // Act
        assertDoesNotThrow(() -> bookService.changeCategories(1L, new BookApiController.UpdateBookCategoryCommand()));

        // Assert
        verify(bookCategoryRepository, times(1)).deleteAll(anyList());
        verify(bookCategoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void changeCategoriesWithEmptyBookCategoryList() {
        // Arrange
        Book testBook = new Book("Test Author", "Test Title", BookStatus.AVAILABLE);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));

        // Act and Assert
        BookCategoryNotFoundException exception = assertThrows(BookCategoryNotFoundException.class,
                () -> bookService.changeCategories(1L, new BookApiController.UpdateBookCategoryCommand()));

        assertEquals("book category is Empty. Category Not Found", exception.getMessage());
        verify(bookCategoryRepository, never()).deleteAll(anyList());
        verify(bookCategoryRepository, never()).saveAll(anyList());
    }
}