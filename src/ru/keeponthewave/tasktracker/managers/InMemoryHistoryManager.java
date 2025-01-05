package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_CAPACITY = 10;
    private final List<Task> historyList = new LinkedList<>();

    @Override
    public int getCapacity() {
        return HISTORY_CAPACITY;
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(historyList);
    }

    @Override
    public void add(Task task) {
        if (historyList.size() == HISTORY_CAPACITY) {
            historyList.removeFirst();
        }
        historyList.add(task);
    }
}
