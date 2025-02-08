package ru.keeponthewave.tasktracker.model;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private final List<Integer> subtaskIds;

    public static EpicTask fromString(String str) {
        String[] splits = str.split(",");
        return new EpicTask(splits[2], splits[4], Integer.parseInt(splits[0]));
    }

    public EpicTask(
            String name,
            String description,
            Integer id
    ) {
        super(name, description, id, TaskStatus.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public SubTask addSubtask(SubTask subTask) {
        subtaskIds.add(subTask.getId());
        return subTask;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }
    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,", id, TaskType.EPIC, name, status, description);
    }
}