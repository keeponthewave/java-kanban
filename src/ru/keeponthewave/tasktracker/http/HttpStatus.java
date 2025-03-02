package ru.keeponthewave.tasktracker.http;

public enum HttpStatus {
    OK(200, "200 Ok"),
    CREATED(201, "201 Created"),
    BAD_REQUEST(400, "400 Bad request"),
    NOT_FOUND(404, "404 Not found"),
    NOT_ACCEPTABLE(406, "406 Not Acceptable"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "501 Not Implemented");

    private final int code;
    private final String title;


    HttpStatus(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}
