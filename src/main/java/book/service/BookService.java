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
import book.repository.BookCategoryRepository;
import book.repository.BookRepository;
import book.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookCategoryRepository bookCategoryRepository;

    public List<ResponseBookDto> getBooksByCategory(Long categoryId) {
        return bookRepository.findAllByCategoryId(categoryId)
                .stream()
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
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
        return book;
    }

    @Transactional
    public ResponseBookDto registerNewBook(BookApiController.CreateBookCommand requestDto) {
        Book book = bookRepository.save(Book.builder()
                        .title(requestDto.getTitle())
                        .author(requestDto.getAuthor())
                        .build());

        List<Category> categories = categoryRepository.findAllById(requestDto.getCategoryIds());
        List<BookCategory> bookCategories = new ArrayList<>();
        for (Category category : categories) {
            bookCategories.add(BookCategory.builder()
                    .book(book)
                    .category(category)
                    .build());
        }

        bookCategoryRepository.saveAll(bookCategories);

        return toDto(book);
    }

    public ResponseBookDto toDto(Book book) {
        return ResponseBookDto.builder()
                .bookId(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .build();
    }

    public ResponseBookDto toDto(Book book, List<Category> category) {
        return ResponseBookDto.builder()
                .bookId(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .categories(category)
                .build();
    }

    @Transactional
    public void changeCategories(Long bookId, BookApiController.UpdateBookCategoryCommand command) {
        Book book = getBook(bookId);

        List<Category> afterCategoryList = categoryRepository.findAllById(command.getCategoryIds());
        List<BookCategory> beforeCategoryList = bookCategoryRepository.findAllByBookId();

        if (beforeCategoryList.size() == 0) {
            log.info("BookCategoryList is Empty, bookId is %d.");
            throw new BookCategoryNotFoundException("book category is Empty. Category Not Found");
        }

        List<Long> commonCategoryIds = new ArrayList<>();
        for (int i = 0; i < afterCategoryList.size(); i++) {
            for (int j = 0; j < beforeCategoryList.size(); j++) {
                if (beforeCategoryList.get(j).getId() == afterCategoryList.get(i).getId()) {
                    commonCategoryIds.add(beforeCategoryList.get(j).getId());
                }
            }
        }

        List<Category> commonCategories = categoryRepository.findAllById(commonCategoryIds);
        List<BookCategory> deleteBookCategories = bookCategoryRepository.findAllByBookIdAndNotInCategoryIds(book, commonCategories);
        List<BookCategory> insertBookCategories = new ArrayList<>();

        for (Category category : afterCategoryList) {
            if(commonCategoryIds.contains(category.getId())){
                insertBookCategories.add(BookCategory.builder()
                        .book(book)
                        .category(category)
                        .build());
            }
        }

        bookCategoryRepository.deleteAll(deleteBookCategories);
        bookCategoryRepository.saveAll(insertBookCategories);
    }
}
