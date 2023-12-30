package book.dto;

import book.entity.Category;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBookDto {
    private Long bookId;
    private String title;
    private String author;
}
