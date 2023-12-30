package book.dto;

import book.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RequestBookDto {
    private String title;
    private String author;
    // 클라이언트에서 categoryId를 줄지, categoryName 줄지, 일단 ID를 주는 것으로 결정
    private List<Long> categoryIds;

}
