package ru.keeponthewave.tasktracker.application.managers;

import ru.keeponthewave.tasktracker.core.abstractive.EpicSpecificManager;
import ru.keeponthewave.tasktracker.core.model.EpicTask;
import ru.keeponthewave.tasktracker.core.model.SubTask;
import ru.keeponthewave.tasktracker.persistance.BaseTaskRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class EpicTaskManager implements EpicSpecificManager {
    private BaseTaskRepository<EpicTask> taskRepository;
    private BaseTaskRepository<SubTask> subTaskRepository;

    public EpicTaskManager(BaseTaskRepository<EpicTask> taskRepository, BaseTaskRepository<SubTask> subTaskRepository) {
        this.taskRepository = taskRepository;
        this.subTaskRepository = subTaskRepository;
    }

    @Override
    public Collection<SubTask> getSubTasks(EpicTask task) {
        return task.getSubtasks();
    }

    @Override
    public Collection<EpicTask> getAllTasks() {
        return taskRepository.getAll();
    }

    @Override
    public EpicTask getTaskById(UUID id) {
        return taskRepository.getById(id);
    }

    @Override
    public EpicTask createTask(EpicTask task) {
        return taskRepository.create(task);
    }

    @Override
    public EpicTask updateTask(EpicTask task) {
        EpicTask existing = taskRepository.getById(task.getId());
        existing.setDescription(task.getDescription());
        existing.setSubtasks(task.getSubtasks());
        existing.setName(task.getName());
        existing.recalculateStatus();

        return taskRepository.update(existing);
    }

    @Override
    public UUID deleteTaskById(UUID id) {
        EpicTask existing = taskRepository.getById(id);
        for (SubTask subtask : existing.getSubtasks()) {
            subTaskRepository.delete(subtask.getId());
        }

        return taskRepository.delete(id);
    }

    @Override
    public void deleteAllTasks() {
        for (EpicTask epicTask : taskRepository.getAll()) {
            deleteTaskById(epicTask.getId());
        }
    }
}
