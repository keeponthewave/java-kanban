package ru.keeponthewave.tasktracker.exceptions;

import java.nio.file.Path;

public class FileManagerRestoreException extends RuntimeException {
    private final Path file;
    private final int line;

    public FileManagerRestoreException(String message, int line, Path file) {
        super(message);
        this.file = file;
        this.line = line;
    }

    @Override
    public String getMessage() {
      return String.format("In file: %s, line %d: %s", file.getFileName(), line , super.getMessage());
    }
}
