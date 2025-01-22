package ru.keeponthewave.tasktracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.util.List;
import java.util.NoSuchElementException;

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
        assertEquals(1, history.size(), "История пустая");
        assertEquals(1, task.getId(), "История сохранена неверно");
    }

    @Test
    void shouldCorrectlyAddSimpleTaskToHistoryWhenAddCopyOfTask() {
        for (int i = 0; i < 10; i++) {
            Task task = new Task("Test task", "it's test task", i, TaskStatus.NEW);
            historyManager.add(task);
        }

        historyManager.add(new Task("Copy task", "it's test task", 5, TaskStatus.NEW));

        List<Task> history = historyManager.getHistory();
        assertEquals(history.size(), 10);
        assertEquals(history.get(5).getId(), 6);
        assertEquals(history.getLast().getId(), 5);
    }

    @Test
    void shouldCorrectlyRemoveTasksFromHistory() {


        for (int i = 0; i < 5; i++) {
            Task task = new Task("Test task", "it's test task", i, TaskStatus.NEW);
            historyManager.add(task);
        }

        for (int i = 4; i > 1; i--) {
            historyManager.remove(i);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(history.size(), 2);
        assertEquals(history.getFirst().getId(), 0);
        assertEquals(history.getLast().getId(), 1);

        for (int i = 1; i >= 0; i--) {
            historyManager.remove(i);
        }

        history = historyManager.getHistory();

        assertEquals(history.size(), 0);
    }

    @Test
    void shouldThrowExceptionWhenRemoveNonExistingTask() {
        assertThrows(NoSuchElementException.class, () -> historyManager.remove(10));
    }

}