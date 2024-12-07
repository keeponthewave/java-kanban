package ru.keeponthewave.tasktracker.core.abstractive;

import ru.keeponthewave.tasktracker.core.model.Task;

import java.util.Collection;
import java.util.UUID;

public interface TaskManager<T extends Task> {
    Collection<T> getAllTasks();

    T getTaskById(UUID id);

    T createTask(T task);

    T updateTask(T task);

    UUID deleteTaskById(UUID id);

    void deleteAllTasks();
}
