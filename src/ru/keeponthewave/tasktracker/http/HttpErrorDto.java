package ru.keeponthewave.tasktracker.http;

public class HttpErrorDto {
    private final String title;
    private final String message;
    private final int code;

    public HttpErrorDto(String title, String message, HttpStatus status) {
        this.title = title;
        this.message = message;
        this.code = status.getCode();
    }

    public HttpErrorDto(HttpStatus status, String message) {
        this.title = status.getTitle();
        this.message = message;
        this.code = status.getCode();
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
