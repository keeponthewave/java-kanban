package ru.keeponthewave.tasktracker.model;

import java.time.Duration;
import java.time.Instant;

public class SubTask extends Task {
    private Integer epicTaskId;

    public SubTask(String name, String description, Integer id, TaskStatus status, Integer epicTaskId, Instant startTime, Duration duration) {
        super(name, description, id, status, startTime, duration);
        type = TaskType.SUBTASK;
        this.epicTaskId = epicTaskId;
    }

    public static SubTask fromString(String str) {
        TaskParams taskParams = Task.taskParamsFromString(str);
        return new SubTask(taskParams.name(), taskParams.description(), taskParams.id(), taskParams.status(),
                taskParams.epicId(), taskParams.startTime(), taskParams.duration());
    }

    public Integer getEpicTaskId() {
        return epicTaskId;
    }

    @Override
    public String toString() {
        return super.toString() + epicTaskId;
    }
}
