package book.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BookNotFoundException  extends RuntimeException{
    private final HttpStatus status;
    private final String message;

    public BookNotFoundException(String message) {
        this.status = HttpStatus.NOT_FOUND;
        this.message = message;
    }

}
