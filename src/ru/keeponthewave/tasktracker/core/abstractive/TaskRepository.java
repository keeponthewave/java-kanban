package ru.keeponthewave.tasktracker.core.abstractive;

import ru.keeponthewave.tasktracker.core.model.Task;

import java.util.Collection;
import java.util.UUID;

abstract public interface TaskRepository<T extends Task> {
    Collection<T> getAll();
    T getById(UUID id);
    T create(T task);
    T update(T task);
    UUID delete(UUID id);
    void clear();
}
