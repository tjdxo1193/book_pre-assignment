package book.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BookCategoryNotFoundException extends RuntimeException{
    private final HttpStatus status;
    private final String message;

    public BookCategoryNotFoundException(String message) {
        this.status = HttpStatus.NOT_FOUND;
        this.message = message;
    }

}
