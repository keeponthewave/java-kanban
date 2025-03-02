package ru.keeponthewave.tasktracker.exceptions;

public class HttpServerInitializationError extends Error {
    public HttpServerInitializationError(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpServerInitializationError(String message) {
        super(message);
    }
}
