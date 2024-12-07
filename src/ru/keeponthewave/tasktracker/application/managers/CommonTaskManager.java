package ru.keeponthewave.tasktracker.application.managers;

import ru.keeponthewave.tasktracker.core.abstractive.TaskManager;
import ru.keeponthewave.tasktracker.core.model.Task;
import ru.keeponthewave.tasktracker.persistance.BaseTaskRepository;

import java.util.Collection;
import java.util.UUID;

public class CommonTaskManager implements TaskManager<Task> {
    private BaseTaskRepository<Task> taskRepository;

    public CommonTaskManager(BaseTaskRepository<Task> taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Collection<Task> getAllTasks() {
        return taskRepository.getAll();
    }

    @Override
    public Task getTaskById(UUID id) {
        return taskRepository.getById(id);
    }

    @Override
    public Task createTask(Task task) {
        return taskRepository.create(task);
    }

    @Override
    public Task updateTask(Task task) {
        return taskRepository.update(task);
    }

    @Override
    public UUID deleteTaskById(UUID id) {
        return taskRepository.delete(id);
    }

    @Override
    public void deleteAllTasks() {
        taskRepository.clear();
    }
}
