package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> taskMap = new HashMap<>();
    private final HashMap<Integer, EpicTask> epicTaskMap = new HashMap<>();
    private final HashMap<Integer, SubTask> subTaskMap = new HashMap<>();

    private final HistoryManager historyManager;

    private Integer idCounter = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public Collection<Task> getAllTasks() {
        return taskMap.values();
    }

    @Override
    public Task getTaskById(int id) {
        checkTaskExistsInStorage(id, taskMap);
        Task task = taskMap.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Task createTask(Task task) {
        task.setId(generateId());
        return taskMap.put(task.getId(), task);
    }

    @Override
    public Task updateTask(Task task) {
        checkTaskExistsInStorage(task.getId(), taskMap);
        return taskMap.put(task.getId(), task);
    }

    @Override
    public int deleteTaskById(int id) {
        checkTaskExistsInStorage(id, taskMap);
        taskMap.remove(id);
        return id;
    }

    @Override
    public void deleteAllTasks() {
        taskMap.clear();
    }

    @Override
    public Collection<SubTask> getAllSubTasks() {
        return subTaskMap.values();
    }

    @Override
    public SubTask getSubTaskById(int id) {
        checkTaskExistsInStorage(id, subTaskMap);
        SubTask task = subTaskMap.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public SubTask createSubTask(SubTask task) {
        checkTaskExistsInStorage(task.getEpicTaskId(), epicTaskMap);

        task.setId(generateId());
        subTaskMap.put(task.getId(), task);

        var epic = epicTaskMap.get(task.getEpicTaskId());
        epic.getSubtaskIds().add(task.getId());
        recalculateEpicStatus(epic);

        return task;
    }

    @Override
    public SubTask updateSubTask(SubTask task) {
        checkTaskExistsInStorage(task.getId(), subTaskMap);
        subTaskMap.put(task.getId(), task);

        var epic = epicTaskMap.get(task.getEpicTaskId());
        recalculateEpicStatus(epic);

        return task;
    }

    @Override
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

    @Override
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

    @Override
    public Collection<SubTask> getEpicSubTasks(EpicTask task) {
        return task.getSubtaskIds()
                .stream()
                .map(subTaskMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Collection<EpicTask> getAllEpicTasks() {
        return epicTaskMap.values();
    }

    @Override
    public EpicTask getEpicTaskById(int id) {
        checkTaskExistsInStorage(id, epicTaskMap);
        EpicTask task = epicTaskMap.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public EpicTask createEpicTask(EpicTask task) {
        task.setId(generateId());
        epicTaskMap.put(task.getId(), task);
        return task;
    }

    @Override
    public EpicTask updateEpicTask(EpicTask task) {
        checkTaskExistsInStorage(task.getId(), epicTaskMap);

        var existing = epicTaskMap.get(task.getId());
        existing.setDescription(task.getDescription());
        existing.setName(task.getName());

        return existing;
    }

    @Override
    public EpicTask deleteEpicTaskById(int id) {
        checkTaskExistsInStorage(id, epicTaskMap);
        EpicTask existing = getEpicTaskById(id);
        existing.getSubtaskIds()
                .stream()
                .filter(Objects::nonNull)
                .forEach(subTaskMap::remove);

        return epicTaskMap.remove(id);
    }

    @Override
    public void deleteAllEpicTasks() {
        epicTaskMap.clear();
        subTaskMap.clear();
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(historyManager.getHistory());
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
        epicTask.setStatus(TaskStatus.IN_PROGRESS);
        return TaskStatus.IN_PROGRESS;
    }

    private <T extends Task> void checkTaskExistsInStorage(Integer taskId, HashMap<Integer, T> storage) {
        if (taskId == null || !storage.containsKey(taskId)) {
            throw new NoSuchElementException(String.format("Задачи с id=%s не существует.", taskId));
        }
    }
}
