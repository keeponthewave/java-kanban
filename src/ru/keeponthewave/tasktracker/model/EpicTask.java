package ru.keeponthewave.tasktracker.model;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private final List<Integer> subtaskIds;

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

}