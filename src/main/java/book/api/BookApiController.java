package book.api;

import book.common.CommonResponse;
import book.dto.RequestBookDto;
import book.dto.ResponseBookDto;
import book.enums.BookStatus;
import book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@Tag(name = "Book API")
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookApiController {

    private final BookService bookService;

    /*
- 도서는 하나 이상의 카테고리에 속할 수 있다.
=> 도서가 만들어 질때 카테고리 List를 받는다. List 길이는 한개 이상이다.
- 도서는 지은이, 제목의 정보를 가지고 있다.
=> 도서 요청파라미터Dto 벨리데이트
- 신규 도서는 항상 카테고리가 필요하다.
=> create book - category list length minimum 1
- 도서는 훼손 또는 분실 등의 이유로 대여가 중단 될 수 있다.
=> 훼손, 분실 == 대여 중단
- 도서는 카테고리가 변경될 수 있다.
=> 도서는 카테고리 변경
- 카테고리 별로 도서를 검색 할 수 있다.
=> select where category - name
- 지은이와 제목으로 도서를 검색 할 수 있다.
=> select where author And title
- 현재 서점에 있는 도서 목록은 다음과 같다.
     */

    @Operation(summary = "카테고리 별로 검색")
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<ResponseBookDto>> getBooksByCategory(@PathVariable Long categoryId) {
        List<ResponseBookDto> books = bookService.getBooksByCategory(categoryId);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "제목 및 지은이로 검색")
    @GetMapping("/by-author-and-title")
    public ResponseEntity<List<ResponseBookDto>> getBooksByAuthorAndTitle(RequestBookDto requestDto) {
        List<ResponseBookDto> books = bookService.getBooksByAuthorAndTitle(requestDto);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "신규 등록 , 등록된 도서 정보 반환")
    @PostMapping
    public ResponseEntity<ResponseBookDto> createBook(@Valid CreateBookCommand command) {
        return ResponseEntity.ok(bookService.registerNewBook(command));
    }


    @Operation(summary = "도서 대여 중단, 활성화")
    @PatchMapping("/{bookId}/changeStatus/{status}")
    public ResponseEntity<CommonResponse> changeBookStatus(@PathVariable Long bookId, @PathVariable BookStatus status) {
        bookService.changeBookStatus(bookId, status);
        return ResponseEntity.ok(new CommonResponse());
    }

    /*
        카테고리 변경
     변경 하는 것에는 여러가지 방법이 있다.방법이
     soft delete, hard delete, 전부 삭제하고 요청된 것 insert 하는 방식, 현재 있는 것과 비교해서 없는것만 insert하고 빠진거는 delete하는 방식
     이 중에 고민했는데, soft delete를 하기에는 부수적인 코드가 많이 사용될 것 같고, hard delete이긴한데, 전부 삭제하는건 나중에 이력이라도 쌓을때 문제가 된다.
     그래서 있는 것과 비교하여 없는 것만 insert 하고, 빠진것은 delete 하는 방식으로 카테고리 변경을 진행하려고한다.
     */
    @Operation(summary = "카테고리 변경")
    @PutMapping("{bookId}/categories")
    public ResponseEntity<CommonResponse> changeCategories(@PathVariable Long bookId, @Valid UpdateBookCategoryCommand command) {
        bookService.changeCategories(bookId, command);
        return ResponseEntity.ok(new CommonResponse());
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public
    class CreateBookCommand {
        @NonNull
        private String title;
        @NonNull
        private String author;

        // id만 받아서 넣을지, category 자체(id, name)를 받아서 넣는지는 전자로 결정했다 치고
        @Size(min = 1)
        private List<Long> categoryIds = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class UpdateBookCategoryCommand {
        // 보통 삭제된 id list와 추가된 id list를 주는 것 같은데(내가 프론트면 그럴거같다)
        // 근데 그냥 이렇게 바꿔달라고 list전체를 줄수도 있는 상황이기 떄문에.. 그때 마다 객체 멤버변수는 달라질 수 있다.
        // 변경된 카테고리 ID list
        @Size(min = 1)
        private List<Long> categoryIds = new ArrayList<>();
    }
}

