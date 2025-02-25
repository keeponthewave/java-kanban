package ru.keeponthewave.tasktracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.exceptions.TimeIntersectionException;
import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    T taskManager;
    EpicTask epic;

    @BeforeEach
    abstract public void prepare();

    @Test
    void getAllTasks() {
        Task task1 = new Task("Test task", "it's test task", 0, TaskStatus.NEW, null, null);
        Task task2 = new Task("Test task", "it's test task", 1, TaskStatus.NEW, null, null);

        assertEquals(taskManager.getAllTasks().size(), 0);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        List<Task> allTasks = taskManager
                .getAllTasks()
                .stream()
                .toList();

        assertEquals(allTasks.size(), 2);
        assertEquals(allTasks.getFirst(), task1);
        assertEquals(allTasks.getLast(), task2);
    }

    @Test
    void getTaskById() {
        Task task = new Task("Test task", "it's test task", 10, TaskStatus.NEW, null, null);
        taskManager.createTask(task);
        assertEquals(taskManager.getTaskById(task.getId()), task);
    }

    @Test
    void updateTask() {
        Task task = new Task("Test task", "it's test task", 0, TaskStatus.NEW, null, null);
        taskManager.createTask(task);
        Task updatedTask = new Task("Test task", "it's updatedTask task", task.getId(), TaskStatus.IN_PROGRESS, null, null);
        taskManager.updateTask(updatedTask);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertEquals(savedTask.getStatus(), updatedTask.getStatus());
        assertEquals(savedTask.getDescription(), updatedTask.getDescription());
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Test task", "it's test task", 0, TaskStatus.NEW, null, null);
        taskManager.createTask(task);
        taskManager.deleteTaskById(task.getId());
        assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(0));
    }

    @Test
    void deleteAllTasks() {
        Task task1 = new Task("Test task", "it's test task", 0, TaskStatus.NEW, null, null);
        Task task2 = new Task("Test task", "it's test task", 1, TaskStatus.NEW, null, null);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.deleteAllTasks();
        assertEquals(taskManager.getAllTasks().size(), 0);
    }

    @Test
    void createSubTask() {
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, -1, null, null);
        assertThrows(NoSuchElementException.class, () -> taskManager.createSubTask(task1));

        SubTask task2 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        taskManager.createSubTask(task2);
        assertTrue(epic.getSubtaskIds().stream().anyMatch(id -> Objects.equals(id, task2.getId())));
    }

    @Test
    void getAllSubTasks() {
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId(), null, null);

        assertEquals(taskManager.getAllSubTasks().size(), 0);
        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);

        List<SubTask> allTasks = taskManager
                .getAllSubTasks()
                .stream()
                .toList();

        assertEquals(allTasks.size(), 2);
        assertEquals(allTasks.getFirst().getId(), task1.getId());
        assertEquals(allTasks.getLast().getId(), task2.getId());
    }

    @Test
    void getSubTaskById() {
        SubTask task = new SubTask("Test task", "it's test task", 10, TaskStatus.NEW, epic.getId(), null, null);
        taskManager.createSubTask(task);
        assertEquals(taskManager.getSubTaskById(task.getId()), task);
    }

    @Test
    void updateSubTask() {
        SubTask task = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        taskManager.createSubTask(task);
        SubTask updatedTask = new SubTask("Test task", "it's updatedTask task", null,
                TaskStatus.IN_PROGRESS, epic.getId(), null, null);

        assertThrows(NoSuchElementException.class, () -> taskManager.updateSubTask(updatedTask));

        updatedTask.setId(task.getId());
        taskManager.updateSubTask(updatedTask);

        Task savedTask = taskManager.getSubTaskById(task.getId());

        assertEquals(savedTask.getStatus(), updatedTask.getStatus());
        assertEquals(savedTask.getDescription(), updatedTask.getDescription());
    }

    @Test
    void deleteSubTaskById() {
        SubTask task = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        taskManager.createSubTask(task);
        taskManager.deleteSubTaskById(task.getId());
        assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(0));
        assertEquals(epic.getSubtaskIds().size(), 0);
    }

    @Test
    void deleteAllSubTasks() {
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId(), null, null);
        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);
        taskManager.deleteAllSubTasks();
        assertEquals(taskManager.getAllSubTasks().size(), 0);
        assertEquals(epic.getSubtaskIds().size(), 0);
    }

    @Test
    void getEpicSubTasks() {
        assertEquals(taskManager.getEpicSubTasks(epic).size(), 0);

        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId(), null, null);
        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);

        assertEquals(taskManager.getEpicSubTasks(epic).size(), 2);
        assertTrue(taskManager.getEpicSubTasks(epic).stream().anyMatch(t -> Objects.equals(task1, t)));
        assertTrue(taskManager.getEpicSubTasks(epic).stream().anyMatch(t -> Objects.equals(task2, t)));
    }

    @Test
    void getAllEpicTasks() {
        assertEquals(taskManager.getAllEpicTasks().size(), 1);

        EpicTask task1 = new EpicTask("Test task", "it's test task", 0);
        EpicTask task2 = new EpicTask("Test task", "it's test task", 1);
        taskManager.createEpicTask(task1);
        taskManager.createEpicTask(task2);

        List<EpicTask> allTasks = taskManager
                .getAllEpicTasks()
                .stream()
                .toList()
                ;

        assertEquals(allTasks.size(), 3);
    }

    @Test
    void getEpicTaskById() {
        assertEquals(taskManager.getEpicTaskById(epic.getId()), epic);
        assertThrows(NoSuchElementException.class, () -> taskManager.getEpicTaskById(-1));
    }

    @Test
    void updateEpicTask() {
        EpicTask updatedTask = new EpicTask("Test task", "it's updatedTask task", epic.getId());
        updatedTask.setStatus(TaskStatus.DONE);

        taskManager.updateEpicTask(updatedTask);

        Task savedTask = taskManager.getEpicTaskById(epic.getId());
        assertNotEquals(savedTask.getStatus(), updatedTask.getStatus());
        assertEquals(savedTask.getDescription(), updatedTask.getDescription());
        assertEquals(savedTask.getName(), updatedTask.getName());
    }

    @Test
    void deleteEpicTaskById() {
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId(), null, null);
        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);
        assertEquals(taskManager.getAllSubTasks().size(), 2);

        taskManager.deleteEpicTaskById(epic.getId());
        assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(0));
        assertEquals(taskManager.getAllSubTasks().size(), 0);
    }

    @Test
    void deleteAllEpicTasks() {
        EpicTask epic2 = new EpicTask("Test task", "it's updatedTask task", epic.getId());
        taskManager.createEpicTask(epic2);

        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic2.getId(), null, null);
        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);
        assertEquals(taskManager.getAllSubTasks().size(), 2);

        taskManager.deleteAllEpicTasks();
        assertEquals(taskManager.getAllSubTasks().size(), 0);
        assertEquals(taskManager.getAllEpicTasks().size(), 0);
    }

    @Test
    void getHistory() {
        SubTask subtask = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        Task task = new Task("Test task", "it's test task", 1, TaskStatus.NEW, null, null);
        taskManager.createTask(task);
        taskManager.createSubTask(subtask);

        taskManager.getEpicTaskById(epic.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getSubTaskById(subtask.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(history.getFirst(), epic);
        assertEquals(history.get(1), task);
        assertEquals(history.getLast(), subtask);
    }

    @Test
    void shouldHistoryCorrectlyWorkWhenTwiceGetTask() {
        SubTask subtask = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        Task task = new Task("Test task", "it's test task", 1, TaskStatus.NEW, null, null);
        taskManager.createTask(task);
        taskManager.createSubTask(subtask);

        taskManager.getEpicTaskById(epic.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getSubTaskById(subtask.getId());
        taskManager.getEpicTaskById(epic.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(history.get(0), task);
        assertEquals(history.get(1), subtask);
        assertEquals(history.get(2), epic);
    }

    @Test
    void shouldCorrectChangeEpicStatus() {
        assertEquals(epic.getStatus(), TaskStatus.NEW);

        SubTask subtask1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId(), null, null);
        SubTask subtask2 = new SubTask("Test task", "it's test task", 0, TaskStatus.IN_PROGRESS, epic.getId(), null, null);

        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        assertEquals(epic.getStatus(), TaskStatus.IN_PROGRESS);

        SubTask subtask2Updated = new SubTask("Test task", "it's test task", subtask2.getId(), TaskStatus.DONE, epic.getId(), null, null);
        taskManager.updateSubTask(subtask2Updated);
        assertEquals(epic.getStatus(), TaskStatus.IN_PROGRESS);

        SubTask subtask1Updated = new SubTask("Test task", "it's test task", subtask1.getId(), TaskStatus.DONE, epic.getId(), null, null);
        taskManager.updateSubTask(subtask1Updated);
        assertEquals(epic.getStatus(), TaskStatus.DONE);
    }

    @Test
    void shouldThrowWhenAddTaskWithTimeIntersection() {
        Instant now = Instant.now();

        Task baseTask = new Task("Test task", "it's test task", 0, TaskStatus.NEW, now, Duration.ofMinutes(10));
        taskManager.createTask(baseTask);

        Task conflictTask1 = new Task("Test task", "it's test task", 1, TaskStatus.NEW, now.plus(Duration.ofMinutes(5)), Duration.ofMinutes(5));
        Task conflictTask2 = new Task("Test task", "it's test task", 1, TaskStatus.NEW, now.minus(Duration.ofMinutes(5)), Duration.ofMinutes(10));
        Task conflictTask3 = new Task("Test task", "it's test task", 1, TaskStatus.NEW, now.plus(Duration.ofMinutes(2)), Duration.ofMinutes(2));
        Task conflictTask4 = new Task("Test task", "it's test task", 1, TaskStatus.NEW, now.minus(Duration.ofMinutes(2)), Duration.ofMinutes(14));

        assertThrows(TimeIntersectionException.class, () -> taskManager.createTask(conflictTask1));
        assertThrows(TimeIntersectionException.class, () -> taskManager.createTask(conflictTask2));
        assertThrows(TimeIntersectionException.class, () -> taskManager.createTask(conflictTask3));
        assertThrows(TimeIntersectionException.class, () -> taskManager.createTask(conflictTask4));
    }

    @Test
    void shouldCorrectCalcEpicTime() {
        var now = Instant.now();
        var INTERVAL = 5;

        var sub1 = new SubTask("sub1", "", 1, TaskStatus.NEW, epic.getId(), now, Duration.ofMinutes(INTERVAL));
        var sub2 = new SubTask("sub1", "", 1, TaskStatus.IN_PROGRESS, epic.getId(), now.plus(Duration.ofMinutes(INTERVAL).plusMinutes(1)), Duration.ofMinutes(INTERVAL));

        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
        assertNull(epic.getDuration());

        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        assertEquals(epic.getStartTime(), sub1.getStartTime());
        assertEquals(epic.getEndTime(), sub2.getEndTime());
        assertEquals(epic.getDuration(), sub2.getDuration().plus(sub1.getDuration()));
    }

    @Test
    void shouldFillPrioritizedTasksWhenAddTaskAndSubTask() {
        var now = Instant.now();
        var INTERVAL = 5;

        var t1 = new Task("task1", "", 1, TaskStatus.NEW, now, Duration.ofMinutes(INTERVAL));
        var sub1 = new SubTask("sub1", "", 1, TaskStatus.NEW, epic.getId(), now.minus(Duration.ofMinutes(INTERVAL + 1)), Duration.ofMinutes(INTERVAL));
        var sub2 = new SubTask("sub2", "", 1, TaskStatus.IN_PROGRESS, epic.getId(), now.plus(Duration.ofMinutes(INTERVAL + 1)), Duration.ofMinutes(INTERVAL));

        assertEquals(taskManager.getPrioritizedTasks().size(), 0);

        taskManager.createTask(t1);
        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        assertEquals(taskManager.getPrioritizedTasks().size(), 3);
        assertEquals(taskManager.getPrioritizedTasks().getFirst(), sub1);
        assertEquals(taskManager.getPrioritizedTasks().getLast(), sub2);
    }

    @Test
    void shouldUpdatePrioritizedTasksWhenUpdateTask() {
        var now = Instant.now();
        var INTERVAL = 5;

        var t1 = new Task("task1", "", 1, TaskStatus.NEW, now, Duration.ofMinutes(INTERVAL));
        var sub1 = new SubTask("sub1", "", 1, TaskStatus.NEW, epic.getId(), now.minus(Duration.ofMinutes(INTERVAL + 1)), Duration.ofMinutes(INTERVAL));
        var sub2 = new SubTask("sub2", "", 1, TaskStatus.IN_PROGRESS, epic.getId(), now.plus(Duration.ofMinutes(INTERVAL + 1)), Duration.ofMinutes(INTERVAL));

        taskManager.createTask(t1);
        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        assertEquals(taskManager.getPrioritizedTasks().size(), 3);
        assertEquals(taskManager.getPrioritizedTasks().getFirst(), sub1);
        assertEquals(taskManager.getPrioritizedTasks().getLast(), sub2);

        var updSub2 = new SubTask("sub2 upd", "", sub2.getId(), TaskStatus.DONE, epic.getId(), sub1.getStartTime().minus(Duration.ofMinutes(INTERVAL * 2)), Duration.ofMinutes(INTERVAL));

        taskManager.updateSubTask(updSub2);

        assertEquals(taskManager.getPrioritizedTasks().size(), 3);
        assertEquals(taskManager.getPrioritizedTasks().getFirst(), updSub2);
        assertEquals(taskManager.getPrioritizedTasks().getLast(), t1);
    }
}
