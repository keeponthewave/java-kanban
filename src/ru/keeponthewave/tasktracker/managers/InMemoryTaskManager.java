package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.exceptions.TimeIntersectionException;
import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> taskMap = new HashMap<>();
    protected final Map<Integer, EpicTask> epicTaskMap = new HashMap<>();
    protected final Map<Integer, SubTask> subTaskMap = new HashMap<>();
    protected final Set<Task> prioritizedTaskSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    private final HistoryManager historyManager;

    protected Integer idCounter = 0;

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
        if (canPrioritized(task)) {
            if (hasTimeIntersection(task, prioritizedTaskSet.stream())) {
                throw new TimeIntersectionException("Ошибка создания задачи: На заданное время уже запланирована задача.");
            }
            prioritizedTaskSet.add(task);
        }
        return taskMap.put(task.getId(), task);
    }

    @Override
    public Task updateTask(Task task) {
        checkTaskExistsInStorage(task.getId(), taskMap);
        if (
                canPrioritized(task)
                        && hasTimeIntersection(task,
                        prioritizedTaskSet.stream().filter(pt -> !Objects.equals(pt.getId(), task.getId())))
        ) {
            throw new TimeIntersectionException("Ошибка обновления задачи: На заданное время уже запланирована задача.");
        }

        var alreadyPrioritized = prioritizedTaskSet
                .stream()
                .filter(t -> Objects.equals(t.getId(), task.getId()))
                .findFirst();
        alreadyPrioritized.ifPresent(prioritizedTaskSet::remove);

        if (canPrioritized(task)) {
            prioritizedTaskSet.add(task);
        }

        return taskMap.put(task.getId(), task);
    }

    @Override
    public int deleteTaskById(int id) {
        checkTaskExistsInStorage(id, taskMap);

        var alreadyPrioritized = prioritizedTaskSet
                .stream()
                .filter(t -> Objects.equals(t.getId(), id))
                .findFirst();
        alreadyPrioritized.ifPresent(prioritizedTaskSet::remove);

        taskMap.remove(id);
        return id;
    }

    @Override
    public void deleteAllTasks() {
        taskMap.values().stream().filter(prioritizedTaskSet::contains).forEach(prioritizedTaskSet::remove);
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

        if (canPrioritized(task)) {
            if (hasTimeIntersection(task, prioritizedTaskSet.stream())) {
                throw new TimeIntersectionException("Ошибка создания задачи: На заданное время уже запланирована задача.");
            }
            prioritizedTaskSet.add(task);
        }
        subTaskMap.put(task.getId(), task);

        var epic = epicTaskMap.get(task.getEpicTaskId());
        epic.getSubtaskIds().add(task.getId());
        recalculateEpicFields(epic);

        return task;
    }

    @Override
    public SubTask updateSubTask(SubTask task) {
        checkTaskExistsInStorage(task.getId(), subTaskMap);
        subTaskMap.put(task.getId(), task);

        if (
                canPrioritized(task)
                        && hasTimeIntersection(task,
                        prioritizedTaskSet.stream().filter(pt -> !Objects.equals(pt.getId(), task.getId())))
        ) {
            throw new TimeIntersectionException("Ошибка обновления задачи: На заданное время уже запланирована задача.");
        }

        var alreadyPrioritized = prioritizedTaskSet
                .stream()
                .filter(t -> Objects.equals(t.getId(), task.getId()))
                .findFirst();
        alreadyPrioritized.ifPresent(prioritizedTaskSet::remove);

        if (canPrioritized(task)) {
            prioritizedTaskSet.add(task);
        }

        var epic = epicTaskMap.get(task.getEpicTaskId());
        recalculateEpicFields(epic);

        return task;
    }

    @Override
    public int deleteSubTaskById(int id) {
        checkTaskExistsInStorage(id, subTaskMap);
        var subTask = subTaskMap.remove(id);

        var alreadyPrioritized = prioritizedTaskSet
                .stream()
                .filter(t -> Objects.equals(t.getId(), id))
                .findFirst();
        alreadyPrioritized.ifPresent(prioritizedTaskSet::remove);

        var epic = epicTaskMap.get(subTask.getEpicTaskId());
        if (epic != null) {
            epic.getSubtaskIds()
                    .remove((Integer) id);
            recalculateEpicFields(epic);
        }
        return id;
    }

    @Override
    public void deleteAllSubTasks() {
        subTaskMap.values().stream().filter(prioritizedTaskSet::contains).forEach(prioritizedTaskSet::remove);
        subTaskMap.values()
                .stream()
                .map(SubTask::getEpicTaskId)
                .map(epicTaskMap::get)
                .filter(Objects::nonNull)
                .forEach(epicTask -> {
                    epicTask.getSubtaskIds().clear();
                    recalculateEpicFields(epicTask);
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

        existing.getSubtaskIds()
                .stream()
                .map(subTaskMap::get)
                .filter(prioritizedTaskSet::contains)
                .forEach(prioritizedTaskSet::remove);

        return epicTaskMap.remove(id);
    }

    @Override
    public void deleteAllEpicTasks() {
        epicTaskMap.clear();

        subTaskMap.values()
                .stream()
                .filter(prioritizedTaskSet::contains)
                .forEach(prioritizedTaskSet::remove);
        subTaskMap.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTaskSet);
    }

    private int generateId() {
        return idCounter++;
    }

    protected void recalculateEpicFields(EpicTask epicTask) {
        recalculateEpicStatus(epicTask);
        recalculateEpicTime(epicTask);
    }

    private void recalculateEpicStatus(EpicTask epicTask) {
        if (epicTask.getSubtaskIds().isEmpty()) {
            epicTask.setStatus(TaskStatus.NEW);
            return;
        }

        for (TaskStatus status : TaskStatus.values()) {
            boolean isAllSubtasksMatchStatus = epicTask.getSubtaskIds()
                    .stream()
                    .map(subTaskMap::get)
                    .allMatch(t -> t.getStatus() == status);

            if (isAllSubtasksMatchStatus) {
                epicTask.setStatus(status);
                return;
            }
        }
        epicTask.setStatus(TaskStatus.IN_PROGRESS);
    }

    private void recalculateEpicTime(EpicTask epicTask) {
        var totalDuration = epicTask.getSubtaskIds()
                    .stream()
                    .map(subTaskMap::get)
                    .filter(subTask -> subTask.getDuration() != null && subTask.getStartTime() != null)
                    .map(SubTask::getDuration)
                    .reduce(Duration.ZERO, (current, acc) -> acc.plus(current));
        var minStartTime = epicTask.getSubtaskIds()
                .stream()
                .map(subTaskMap::get)
                .filter(subTask -> subTask.getDuration() != null && subTask.getStartTime() != null)
                .map(SubTask::getStartTime)
                .min(Instant::compareTo);
        var maxEndTime = epicTask.getSubtaskIds()
                .stream()
                .map(subTaskMap::get)
                .filter(subTask -> subTask.getDuration() != null && subTask.getStartTime() != null)
                .map(subTask -> subTask.getStartTime().plus(subTask.getDuration()))
                .max(Instant::compareTo);
        epicTask.setDuration(totalDuration);
        epicTask.setStartTime(minStartTime.orElse(null));
        epicTask.setEndTime(maxEndTime.orElse(null));
    }

    private void checkTaskExistsInStorage(Integer taskId, Map<Integer, ? extends Task> storage) {
        if (taskId == null || !storage.containsKey(taskId)) {
            throw new NoSuchElementException(String.format("Задачи с id=%s не существует.", taskId));
        }
    }

    private boolean canPrioritized(Task task) {
        return task.getStartTime() != null
                && task.getEndTime() != null
                && task.getStartTime().isBefore(task.getEndTime());
    }

    private boolean hasTimeIntersection(Task another, Stream<Task> storage) {

        BiPredicate<Instant, Instant> isBeforeOrEq = (a, b) -> a.isBefore(b) || a.equals(b);
        BiPredicate<Instant, Instant> isAfterOrEq = (a, b) -> a.isAfter(b) || a.equals(b);

        return storage
            .anyMatch(t -> isBeforeOrEq.test(t.getEndTime(), another.getEndTime()) && isAfterOrEq.test(t.getEndTime(), another.getStartTime())
                    || isBeforeOrEq.test(t.getStartTime(), another.getEndTime()) && isAfterOrEq.test(t.getStartTime(), another.getStartTime())
                    || isBeforeOrEq.test(another.getEndTime(), t.getEndTime()) && isAfterOrEq.test(another.getEndTime(), t.getStartTime())
                    || isBeforeOrEq.test(another.getStartTime(), t.getEndTime()) && isAfterOrEq.test(another.getStartTime(),t.getStartTime())
            );
    }
}
