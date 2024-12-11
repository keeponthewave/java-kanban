package ru.keeponthewave.tasktracker.model;

public class SubTask extends Task {
    private Integer epicTaskId;

    public SubTask(String name, String description, Integer id, TaskStatus status, Integer epicTaskId) {
        super(name, description, id, status);
        this.epicTaskId = epicTaskId;
    }

    public Integer getEpicTaskId() {
        return epicTaskId;
    }
}
