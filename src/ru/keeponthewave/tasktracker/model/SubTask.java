package ru.keeponthewave.tasktracker.model;

import java.util.UUID;

public class SubTask extends Task {
    private EpicTask epicTask;

    public EpicTask getEpicTask() {
        return epicTask;
    }

    public SubTask(String name, String description, Integer id, TaskStatus status, EpicTask epicTask) {
        super(name, description, id, status);
        this.epicTask = epicTask;
    }

    @Override
    public SubTask updateFrom(Task other) {
        return (SubTask) super.updateFrom(other);
    }
}
