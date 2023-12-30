package book.common;

import lombok.Getter;

@Getter
public class CommonResponse {

    private final boolean result;
    private final String message;

    public CommonResponse() {
        this.result = true;
        this.message = null;
    }

    public CommonResponse(boolean result) {
        this.result = result;
        this.message = null;
    }

    public CommonResponse(String message) {
        this.result = true;
        this.message = message;
    }

    public CommonResponse(boolean result, String message) {
        this.result = result;
        this.message = message;
    }
}