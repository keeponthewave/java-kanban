package ru.keeponthewave.tasktracker.persistance;

import ru.keeponthewave.tasktracker.core.abstractive.TaskRepository;
import ru.keeponthewave.tasktracker.core.model.Task;

import java.util.*;

public class BaseTaskRepository<K extends Task> implements TaskRepository<K> {
    private final HashMap<UUID, K> tasksMap = new HashMap<>();
    @Override
    public Collection<K> getAll() {
        return tasksMap.values();
    }

    @Override
    public K getById(UUID id) {
        if (!tasksMap.containsKey(id)) {
            throw new NoSuchElementException(String.format("Задачи с id=%s не существует.", id));
        }
        return tasksMap.get(id);
    }

    @Override
    public K create(K task) {
        UUID id = task.getId();
        if (id == null) {
            task.setId(UUID.randomUUID());
        }
        return tasksMap.put(task.getId(), task);
    }

    @Override
    public K update(K task) {
        if (!tasksMap.containsKey(task.getId())) {
            throw new NoSuchElementException(String.format("Задачи с id=%s не существует.", task.getId()));
        }
        return tasksMap.put(task.getId(), task);
    }

    @Override
    public UUID delete(UUID id) {
        if (!tasksMap.containsKey(id)) {
            throw new NoSuchElementException(String.format("Задачи с id=%s не существует.", id));
        }
        return tasksMap
                .remove(id)
                .getId();
    }

    @Override
    public void clear() {
        tasksMap.clear();
    }
}
