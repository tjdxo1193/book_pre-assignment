package book.service;

import book.api.BookApiController;
import book.dto.RequestBookDto;
import book.dto.ResponseBookDto;
import book.entity.Book;
import book.entity.BookCategory;
import book.entity.Category;
import book.enums.BookStatus;
import book.exception.BookCategoryNotFoundException;
import book.exception.BookNotFoundException;
import book.exception.CategoryNotFoundException;
import book.repository.BookCategoryRepository;
import book.repository.BookRepository;
import book.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookCategoryRepository bookCategoryRepository;

    public List<ResponseBookDto> getBooksByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
        List<Book> bookList = bookRepository.findAllByCategory(category);
        return bookList.stream()
                .map(this::toDto)
                .toList();
    }

    public List<ResponseBookDto> getBooksByAuthorAndTitle(RequestBookDto requestDto) {
        return bookRepository.findAllByAuthorAndTitle(requestDto.getAuthor(), requestDto.getTitle())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void changeBookStatus(Long bookId, BookStatus status) {
        Book book = getBook(bookId);
        book.updateStatus(status);
    }

    private Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
    }

    @Transactional
    public ResponseBookDto registerNewBook(BookApiController.CreateBookCommand requestDto) {
        Book book = bookRepository.save(Book.builder()
                        .title(requestDto.getTitle())
                        .author(requestDto.getAuthor())
                        .build());

        List<Category> categories = categoryRepository.findAllById(requestDto.getCategoryIds());
        if (categories.size() == 0) {
            throw new CategoryNotFoundException("category is NotFound");
        }

        List<BookCategory> bookCategories = new ArrayList<>();
        for (Category category : categories) {
            bookCategories.add(BookCategory.builder()
                    .book(book)
                    .category(category)
                    .build());
        }

        bookCategoryRepository.saveAll(bookCategories);

        return toDto(book, categories);
    }

    public ResponseBookDto toDto(Book book) {
        return ResponseBookDto.builder()
                .bookId(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .build();
    }

    public ResponseBookDto toDto(Book book, List<Category> categories) {
        return ResponseBookDto.builder()
                .bookId(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .categories(categories)
                .build();
    }

    @Transactional
    public void changeCategories(Long bookId, BookApiController.UpdateBookCategoryCommand command) {
        Book book = getBook(bookId);

        List<Category> afterCategoryList = categoryRepository.findAllById(command.getCategoryIds());
        List<BookCategory> beforeCategoryList = bookCategoryRepository.findAllByBook(book);

        if (beforeCategoryList.isEmpty()) {
            log.debug("BookCategoryList is Empty, bookId is {}.", bookId);
            throw new BookCategoryNotFoundException("book category is Empty. Category Not Found");
        }

        List<Long> commonCategoryIds = afterCategoryList.stream()
                .map(Category::getId)
                .filter(categoryId -> beforeCategoryList.stream()
                        .anyMatch(bookCategory -> bookCategory.getCategory().getId().equals(categoryId)))
                .toList();


        List<BookCategory> deleteBookCategories = beforeCategoryList.stream()
                .filter(bookCategory -> !commonCategoryIds.contains(bookCategory.getCategory().getId()))
                .toList();

        List<BookCategory> insertBookCategories = afterCategoryList.stream()
                .filter(category -> !commonCategoryIds.contains(category.getId()))
                .map(category -> BookCategory.builder()
                        .book(book)
                        .category(category)
                        .build())
                .toList();

        bookCategoryRepository.deleteAll(deleteBookCategories);
        bookCategoryRepository.saveAll(insertBookCategories);
    }
}
