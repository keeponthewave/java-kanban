package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;

import java.util.*;

public class InMemoryTaskManager {
    private final HashMap<Integer, Task> taskMap = new HashMap<>();
    private final HashMap<Integer, EpicTask> epicTaskMap = new HashMap<>();
    private final HashMap<Integer, SubTask> subTaskMap = new HashMap<>();

    private Integer idCounter = 0;

    public Collection<Task> getAllTasks() {
        return taskMap.values();
    }

    public Task getTaskById(int id) {
        checkTaskExistsInStorage(id, taskMap);
        return taskMap.get(id);
    }

    public Task createTask(Task task) {
        task.setId(generateId());
        return taskMap.put(task.getId(), task);
    }

    public Task updateTask(Task task) {
        SubTask existing = getSubTaskById(task.getId());
        return existing.updateFrom(task);
    }

    public int deleteTaskById(int id) {
        checkTaskExistsInStorage(id, taskMap);
        taskMap.remove(id);
        return id;
    }

    public void deleteAllTasks() {
        taskMap.clear();
    }

    public Collection<SubTask> getAllSubTasks() {
        return subTaskMap.values();
    }

    public SubTask getSubTaskById(int id) {
        checkTaskExistsInStorage(id, subTaskMap);
        return subTaskMap.get(id);
    }

    public SubTask createSubTask(SubTask task) {
        task.setId(generateId());
        var epic = task.getEpicTask();
        epic.getSubtasks().add(task);
        epic.recalculateStatus();
        return subTaskMap.put(task.getId(), task);
    }

    public SubTask updateSubTask(SubTask task) {
        SubTask existing = getSubTaskById(task.getId());
        var updated = existing.updateFrom(task);
        task.getEpicTask().recalculateStatus();
        return updated;
    }

    public int deleteSubTaskById(int id) {
        var existing = getSubTaskById(id);
        existing.getEpicTask()
                .getSubtasks()
                .remove(existing);
        subTaskMap.remove(id);
        return id;
    }

    public void deleteAllSubTasks() {
        for (SubTask subTask : subTaskMap.values()) {
            var epic = subTask.getEpicTask();
            epic.getSubtasks().clear();
            epic.recalculateStatus();
        }
        subTaskMap.clear();
    }

    public Collection<SubTask> getEpicSubTasks(EpicTask task) {
        return task.getSubtasks();
    }

    public Collection<EpicTask> getAllEpicTasks() {
        return epicTaskMap.values();
    }

    public EpicTask getEpicTaskById(int id) {
        checkTaskExistsInStorage(id, epicTaskMap);
        return epicTaskMap.get(id);
    }

    public EpicTask createEpicTask(EpicTask task) {
        task.setId(generateId());
        return epicTaskMap.put(task.getId(), task);
    }

    public EpicTask updateEpicTask(EpicTask task) {
        EpicTask existing = getEpicTaskById(task.getId());
        return existing.updateFrom(task);
    }

    public EpicTask deleteEpicTaskById(int id) {
        EpicTask existing = getEpicTaskById(id);

        for (SubTask subtask : existing.getSubtasks()) {
            deleteSubTaskById(subtask.getId());
        }

        return epicTaskMap.remove(id);
    }

    public void deleteAllEpicTasks() {
        for (EpicTask epicTask : getAllEpicTasks()) {
            deleteEpicTaskById(epicTask.getId());
        }
    }

    private int generateId() {
        return idCounter++;
    }

    private <T extends Task> void checkTaskExistsInStorage(Integer taskId, HashMap<Integer, T> storage) {
        if (taskId == null || !storage.containsKey(taskId)) {
            throw new NoSuchElementException(String.format("Задачи с id=%s не существует.", taskId));
        }
    }

}
