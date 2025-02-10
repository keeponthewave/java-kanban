package ru.keeponthewave.tasktracker.model;

import java.util.Objects;

public class Task {
    protected String name;
    protected String description;
    protected Integer id;
    protected TaskStatus status;

    public Task(String name, String description, Integer id, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public static Task fromString(String str) {
        String[] splits = str.split(",");
        return new Task(splits[2], splits[4], Integer.parseInt(splits[0]), TaskStatus.valueOf(splits[3]));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public Task setId(Integer id) {
        this.id = id;
        return this;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(getId(), task.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,", id, TaskType.TASK, name, status, description);
    }
}
