package ru.keeponthewave.tasktracker.exceptions;

public class FileManagerSaveException extends RuntimeException {
    public FileManagerSaveException(String message) {
        super(message);
    }
}
