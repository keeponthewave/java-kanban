package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;

import java.util.Collection;
import java.util.List;

public interface TaskManager {
    Collection<Task> getAllTasks();

    Task getTaskById(int id);

    Task createTask(Task task);

    Task updateTask(Task task);

    Task deleteTaskById(int id);

    void deleteAllTasks();

    Collection<SubTask> getAllSubTasks();

    SubTask getSubTaskById(int id);

    SubTask createSubTask(SubTask task);

    SubTask updateSubTask(SubTask task);

    SubTask deleteSubTaskById(int id);

    void deleteAllSubTasks();

    Collection<SubTask> getEpicSubTasks(EpicTask task);

    Collection<EpicTask> getAllEpicTasks();

    EpicTask getEpicTaskById(int id);

    EpicTask createEpicTask(EpicTask task);

    EpicTask updateEpicTask(EpicTask task);

    EpicTask deleteEpicTaskById(int id);

    void deleteAllEpicTasks();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
