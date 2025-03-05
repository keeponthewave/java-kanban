package ru.keeponthewave.tasktracker.http;

public class HttpResult<T> {
    private final HttpStatus status;
    private final T body;

    public HttpResult(HttpStatus status, T body) {
        this.status = status;
        this.body = body;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public T getBody() {
        return body;
    }
}
