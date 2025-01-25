package ru.keeponthewave.tasktracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    public void prepare() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldCorrectlyAddSimpleTaskToHistory() {
        Task task = new Task("Test task", "it's test task", 1, TaskStatus.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "Список истории отсутствует");
        assertEquals( history.size(), 1,"История пустая");
        assertEquals(task.getId(), history.getFirst().getId(), "История сохранена неверно");
    }

    @Test
    void shouldCorrectlyAddSimpleTaskToHistoryWhenAddCopyOfTask() {
        var tasksList = List.of(
                new Task("Simple task", "it's test task", 1, TaskStatus.NEW),
                new Task("Simple task", "it's test task", 2, TaskStatus.NEW)
        );

        for (var task : tasksList) {
            historyManager.add(task);
        }

        List<Task> historyBefore = historyManager.getHistory();
        assertEquals(historyBefore.size(), tasksList.size());

        var copyOfFirstTask = new Task("Copy of first task", "it's test task", 1, TaskStatus.NEW);
        historyManager.add(copyOfFirstTask);

        List<Task> historyAfter = historyManager.getHistory();
        assertEquals(historyAfter.size(), tasksList.size());
        assertEquals(historyAfter.getFirst().getId(), tasksList.get(1).getId());
        assertEquals(historyAfter.getLast().getId(), copyOfFirstTask.getId());
    }

    @Test
    void shouldCorrectlyRemoveTasksFromHistory() {
        var tasksList = List.of(
                new Task("Simple task", "it's test task", 1, TaskStatus.NEW),
                new Task("Simple task", "it's test task", 2, TaskStatus.NEW)
        );

        for (var task : tasksList) {
            historyManager.add(task);
        }

        List<Task> historyBefore = historyManager.getHistory();

        assertEquals(historyBefore.size(), 2);
        assertEquals(historyBefore.getFirst().getId(), tasksList.getFirst().getId());
        assertEquals(historyBefore.getLast().getId(), tasksList.getLast().getId());

        for (var task : tasksList) {
            historyManager.remove(task.getId());
        }

        List<Task> historyAfter = historyManager.getHistory();
        assertEquals(historyAfter.size(), 0);
    }

    @Test
    void shouldHistoryBeLikeBeforeWhenRemoveNonExistingTask() {
        var tasksList = List.of(
                new Task("Simple task", "it's test task", 1, TaskStatus.NEW),
                new Task("Simple task", "it's test task", 2, TaskStatus.NEW)
        );

        for (var task : tasksList) {
            historyManager.add(task);
        }

        List<Task> historyBefore = historyManager.getHistory();

        assertEquals(historyBefore.size(), 2);
        assertEquals(historyBefore.getFirst().getId(), tasksList.getFirst().getId());
        assertEquals(historyBefore.getLast().getId(), tasksList.getLast().getId());

        historyManager.remove(3);

        List<Task> historyAfter = historyManager.getHistory();
        assertEquals(historyAfter.size(), 2);
        assertEquals(historyAfter.getFirst().getId(), tasksList.getFirst().getId());
        assertEquals(historyAfter.getLast().getId(), tasksList.getLast().getId());
    }

}