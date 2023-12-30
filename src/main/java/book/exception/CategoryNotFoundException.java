package book.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CategoryNotFoundException extends RuntimeException{
    private final HttpStatus status;
    private final String message;

    public CategoryNotFoundException(String message) {
        this.status = HttpStatus.NOT_FOUND;
        this.message = message;
    }

}
