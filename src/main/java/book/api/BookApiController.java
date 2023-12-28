package book.api;

import book.dto.ResponseBookDto;
import book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("book")
@RequiredArgsConstructor
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
//    @GetMapping("/books-by-category")
//    public ResponseEntity<List<ResponseBookDto>> getBooks() {
//
//    }
}
