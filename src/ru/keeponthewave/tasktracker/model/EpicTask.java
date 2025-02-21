package ru.keeponthewave.tasktracker.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private final List<Integer> subtaskIds;
    private Instant endTime;

    public static EpicTask fromString(String str) {
        TaskParams taskParams = Task.taskParamsFromString(str);
        return new EpicTask(taskParams.name(), taskParams.description(), taskParams.id());
    }

    public EpicTask(
            String name,
            String description,
            Integer id
    ) {
        super(name, description, id, TaskStatus.NEW, null, null);
        type = TaskType.EPIC;
        this.subtaskIds = new ArrayList<>();
    }

    public SubTask addSubtask(SubTask subTask) {
        subtaskIds.add(subTask.getId());
        return subTask;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    @Override
    public Instant getEndTime() {
        return endTime;
    }
}