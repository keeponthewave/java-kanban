package ru.keeponthewave.tasktracker.application.managers;

import ru.keeponthewave.tasktracker.core.abstractive.TaskManager;
import ru.keeponthewave.tasktracker.core.model.SubTask;
import ru.keeponthewave.tasktracker.persistance.BaseTaskRepository;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;

public class SubTaskManager implements TaskManager<SubTask> {
    private BaseTaskRepository<SubTask> taskRepository;

    public SubTaskManager(BaseTaskRepository<SubTask> taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Collection<SubTask> getAllTasks() {
        return taskRepository.getAll();
    }

    @Override
    public SubTask getTaskById(UUID id) {
        return taskRepository.getById(id);
    }

    @Override
    public SubTask createTask(SubTask task) {
        task.getEpicTask()
                .getSubtasks()
                .add(task);
        return taskRepository.create(task);
    }

    @Override
    public SubTask updateTask(SubTask task) {
        var existingTask = taskRepository.getById(task.getId());

        var updatedTask = taskRepository.update(existingTask);
        updatedTask.getEpicTask().recalculateStatus();

        return updatedTask;
    }

    @Override
    public UUID deleteTaskById(UUID id) {
        var epic = taskRepository
                .getById(id)
                .getEpicTask();

        epic.setSubtasks(
                epic.getSubtasks()
                        .stream()
                        .filter(subTask -> subTask.getId() != id)
                        .toList()
        );
        epic.recalculateStatus();

        return taskRepository.delete(id);
    }

    @Override
    public void deleteAllTasks() {
        for (SubTask subTask : taskRepository.getAll()) {
            var epic = subTask.getEpicTask();
            epic.getSubtasks().clear();
            epic.recalculateStatus();
        }
        taskRepository.clear();
    }
}
