package ru.keeponthewave.tasktracker.model;

public class SubTask extends Task {
    private Integer epicTaskId;

    public static SubTask fromString(String str) {
        String[] splits = str.split(",");
        return new SubTask(splits[2], splits[4], Integer.parseInt(splits[0]), TaskStatus.valueOf(splits[3]), Integer.parseInt(splits[5]));
    }

    public SubTask(String name, String description, Integer id, TaskStatus status, Integer epicTaskId) {
        super(name, description, id, status);
        this.epicTaskId = epicTaskId;
    }

    public Integer getEpicTaskId() {
        return epicTaskId;
    }
    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,", id, TaskType.SUBTASK, name, status, description) + epicTaskId;
    }
}
