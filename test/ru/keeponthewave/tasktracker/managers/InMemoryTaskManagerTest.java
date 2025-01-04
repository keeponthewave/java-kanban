package ru.keeponthewave.tasktracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    TaskManager taskManager;
    EpicTask epic;

    @BeforeEach
    public void prepare() {
        taskManager = Managers.getDefault();
        epic = new EpicTask("epic", "It's ", 10);
        taskManager.createEpicTask(epic);
    }

    @Test
    void getAllTasks() {
        Task task1 = new Task("Test task", "it's test task", 0, TaskStatus.NEW);
        Task task2 = new Task("Test task", "it's test task", 1, TaskStatus.NEW);

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
        Task task = new Task("Test task", "it's test task", 10, TaskStatus.NEW);
        taskManager.createTask(task);
        assertEquals(taskManager.getTaskById(task.getId()), task);
    }

    @Test
    void updateTask() {
        Task task = new Task("Test task", "it's test task", 0, TaskStatus.NEW);
        taskManager.createTask(task);
        Task updatedTask = new Task("Test task", "it's updatedTask task", task.getId(), TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertEquals(savedTask.getStatus(), updatedTask.getStatus());
        assertEquals(savedTask.getDescription(), updatedTask.getDescription());
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Test task", "it's test task", 0, TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.deleteTaskById(task.getId());
        assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(0));
    }

    @Test
    void deleteAllTasks() {
        Task task1 = new Task("Test task", "it's test task", 0, TaskStatus.NEW);
        Task task2 = new Task("Test task", "it's test task", 1, TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.deleteAllTasks();
        assertEquals(taskManager.getAllTasks().size(), 0);
    }

    @Test
    void createSubTask() {
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, -1);
        assertThrows(NoSuchElementException.class, () -> taskManager.createSubTask(task1));

        SubTask task2 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(task2);
        assertTrue(epic.getSubtaskIds().stream().anyMatch(id -> Objects.equals(id, task2.getId())));
    }

    @Test
    void getAllSubTasks() {
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId());

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
        SubTask task = new SubTask("Test task", "it's test task", 10, TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(task);
        assertEquals(taskManager.getSubTaskById(task.getId()), task);
    }

    @Test
    void updateSubTask() {
        SubTask task = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(task);
        SubTask updatedTask = new SubTask("Test task", "it's updatedTask task", null,
                TaskStatus.IN_PROGRESS, epic.getId());

        assertThrows(NoSuchElementException.class, () -> taskManager.updateSubTask(updatedTask));

        updatedTask.setId(task.getId());
        taskManager.updateSubTask(updatedTask);

        Task savedTask = taskManager.getSubTaskById(task.getId());

        assertEquals(savedTask.getStatus(), updatedTask.getStatus());
        assertEquals(savedTask.getDescription(), updatedTask.getDescription());
    }

    @Test
    void deleteSubTaskById() {
        SubTask task = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(task);
        taskManager.deleteSubTaskById(task.getId());
        assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(0));
        assertEquals(epic.getSubtaskIds().size(), 0);
    }

    @Test
    void deleteAllSubTasks() {
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);
        taskManager.deleteAllSubTasks();
        assertEquals(taskManager.getAllSubTasks().size(), 0);
        assertEquals(epic.getSubtaskIds().size(), 0);
    }

    @Test
    void getEpicSubTasks() {
        assertEquals(taskManager.getEpicSubTasks(epic).size(), 0);

        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId());
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
                .toList();

        assertEquals(allTasks.size(), 3);
        assertEquals(allTasks.getFirst(), epic);
        assertEquals(allTasks.getLast(), task2);
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
        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic.getId());
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

        SubTask task1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        SubTask task2 = new SubTask("Test task", "it's test task", 1, TaskStatus.NEW, epic2.getId());
        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);
        assertEquals(taskManager.getAllSubTasks().size(), 2);

        taskManager.deleteAllEpicTasks();
        assertEquals(taskManager.getAllSubTasks().size(), 0);
        assertEquals(taskManager.getAllEpicTasks().size(), 0);
    }

    @Test
    void getHistory() {
        SubTask subtask = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        Task task = new Task("Test task", "it's test task", 1, TaskStatus.NEW);
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
    void shouldCorrectChange() {
        assertEquals(epic.getStatus(), TaskStatus.NEW);

        SubTask subtask1 = new SubTask("Test task", "it's test task", 0, TaskStatus.NEW, epic.getId());
        SubTask subtask2 = new SubTask("Test task", "it's test task", 0, TaskStatus.IN_PROGRESS, epic.getId());

        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        assertEquals(epic.getStatus(), TaskStatus.IN_PROGRESS);

        SubTask subtask2Updated = new SubTask("Test task", "it's test task", subtask2.getId(), TaskStatus.DONE, epic.getId());
        taskManager.updateSubTask(subtask2Updated);
        assertEquals(epic.getStatus(), TaskStatus.IN_PROGRESS);

        SubTask subtask1Updated = new SubTask("Test task", "it's test task", subtask1.getId(), TaskStatus.DONE, epic.getId());
        taskManager.updateSubTask(subtask1Updated);
        assertEquals(epic.getStatus(), TaskStatus.DONE);
    }
}