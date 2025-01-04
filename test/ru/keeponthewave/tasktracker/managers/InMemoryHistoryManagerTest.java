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
        assertEquals(1, history.size(), "История пустая");
        assertEquals(1, task.getId(), "История сохранена неверно");
    }

    @Test
    void shouldCorrectlyAddSimpleTaskToHistoryWhenCapacityOverflow() {
        for (int i = 0; i < historyManager.getCapacity() + 1; i++) {
            Task task = new Task("Test task", "it's test task", i, TaskStatus.NEW);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(history.size(), 10);
        assertEquals(history.getFirst().getId(), 1);
        assertEquals(history.getLast().getId(), historyManager.getCapacity());
    }

}