package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager {
    private final HashMap<Integer, Task> taskMap = new HashMap<>();
    private final HashMap<Integer, EpicTask> epicTaskMap = new HashMap<>();
    private final HashMap<Integer, SubTask> subTaskMap = new HashMap<>();

    private Integer idCounter = 0;

    public InMemoryTaskManager() {
        EpicTask.setSubTaskStorage(subTaskMap);
    }

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
        checkTaskExistsInStorage(task.getId(), taskMap);
        return taskMap.put(task.getId(), task);
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
        checkTaskExistsInStorage(task.getEpicTaskId(), epicTaskMap);

        task.setId(generateId());
        subTaskMap.put(task.getId(), task);

        var epic = epicTaskMap.get(task.getEpicTaskId());
        epic.getSubtaskIds().add(task.getId());
        recalculateEpicStatus(epic);

        return task;
    }

    public SubTask updateSubTask(SubTask task) {
        checkTaskExistsInStorage(task.getId(), subTaskMap);
        subTaskMap.put(task.getId(), task);

        var epic = epicTaskMap.get(task.getEpicTaskId());
        recalculateEpicStatus(epic);

        return task;
    }

    public int deleteSubTaskById(int id) {
        checkTaskExistsInStorage(id, subTaskMap);
        var subTask = subTaskMap.remove(id);

        var epic = epicTaskMap.get(subTask.getEpicTaskId());
        if (epic != null) {
            epic.getSubtaskIds()
                    .remove((Integer) id);
            recalculateEpicStatus(epic);
        }
        return id;
    }

    public void deleteAllSubTasks() {
        subTaskMap.values()
                .stream()
                .map(SubTask::getEpicTaskId)
                .map(epicTaskMap::get)
                .filter(Objects::nonNull)
                .forEach(epicTask -> {
                    epicTask.getSubtaskIds().clear();
                    recalculateEpicStatus(epicTask);
                });
        subTaskMap.clear();
    }

    public Collection<SubTask> getEpicSubTasks(EpicTask task) {
        return task.getSubtaskIds()
                .stream()
                .map(subTaskMap::get)
                .filter(Objects::nonNull)
                .toList();
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
        checkTaskExistsInStorage(task.getId(), epicTaskMap);

        var existing = epicTaskMap.get(task.getId());
        existing.setDescription(task.getDescription());
        existing.setName(task.getName());

        return existing;
    }

    public EpicTask deleteEpicTaskById(int id) {
        checkTaskExistsInStorage(id, epicTaskMap);
        EpicTask existing = getEpicTaskById(id);
        existing.getSubtaskIds()
                .stream()
                .filter(Objects::nonNull)
                .forEach(subTaskMap::remove);

        return epicTaskMap.remove(id);
    }

    public void deleteAllEpicTasks() {
        epicTaskMap.clear();
        subTaskMap.clear();
    }

    private int generateId() {
        return idCounter++;
    }

    private TaskStatus recalculateEpicStatus(EpicTask epicTask) {
        if (epicTask.getSubtaskIds().isEmpty()) {
            return TaskStatus.NEW;
        }

        for (TaskStatus status : TaskStatus.values()) {
            boolean isAllSubtasksMatchStatus = epicTask.getSubtaskIds()
                    .stream()
                    .map(subTaskMap::get)
                    .allMatch(t -> t.getStatus() == status);

            if (isAllSubtasksMatchStatus) {
                epicTask.setStatus(status);
                return status;
            }
        }

        return TaskStatus.IN_PROGRESS;
    }

    private <T extends Task> void checkTaskExistsInStorage(Integer taskId, HashMap<Integer, T> storage) {
        if (taskId == null || !storage.containsKey(taskId)) {
            throw new NoSuchElementException(String.format("Задачи с id=%s не существует.", taskId));
        }
    }

}
