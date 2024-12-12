package ru.keeponthewave.tasktracker.model;

import ru.keeponthewave.tasktracker.exceptions.ForbiddenException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EpicTask extends Task {
    private final List<Integer> subtaskIds;
    private static HashMap<Integer, SubTask> subTaskStorage;

    public EpicTask(
            String name,
            String description,
            Integer id
    ) {
        super(name, description, id, TaskStatus.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public static void setSubTaskStorage(HashMap<Integer, SubTask> subTaskStorage) {
        EpicTask.subTaskStorage = subTaskStorage;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }
}