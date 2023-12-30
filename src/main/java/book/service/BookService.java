package book.service;

import book.api.BookApiController;
import book.dto.RequestBookDto;
import book.dto.ResponseBookDto;
import book.entity.Book;
import book.entity.Category;
import book.enums.BookStatus;
import book.exception.BookNotFoundException;
import book.repository.BookCategoryRepository;
import book.repository.BookRepository;
import book.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
        book.updateStatus(status);
    }

    @Transactional
    public ResponseBookDto registerNewBook(BookApiController.CreateBookCommand requestDto) {
        Book book = bookRepository.save(Book.builder()
                        .title(requestDto.getTitle())
                        .author(requestDto.getAuthor())
                        .build());
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
}
