package ru.keeponthewave.tasktracker.core.model;

import java.util.UUID;

public class SubTask extends Task {
    private EpicTask epicTask;

    public EpicTask getEpicTask() {
        return epicTask;
    }

    public SubTask(String name, String description, UUID id, TaskStatus status, EpicTask epicTask) {
        super(name, description, id, status);
        this.epicTask = epicTask;
    }
}
