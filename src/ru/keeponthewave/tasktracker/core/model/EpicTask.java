package ru.keeponthewave.tasktracker.core.model;

import ru.keeponthewave.tasktracker.core.exceptions.ForbiddenException;

import java.util.List;
import java.util.UUID;

public class EpicTask extends Task {
    private List<SubTask> subtasks;

    public List<SubTask> getSubtasks() {
        return subtasks;
    }

    public EpicTask setSubtasks(List<SubTask> subtasks) {
        this.subtasks = subtasks;
        return this;
    }

    public EpicTask(
            String name,
            String description,
            UUID id,
            TaskStatus status,
            List<SubTask> subtasks
    ) {
        super(name, description, id, status);
        this.subtasks = subtasks;
    }


    @Override
    public void setStatus(TaskStatus status) {
        throw new ForbiddenException("Операция запрещена");
    }

    public TaskStatus recalculateStatus() {
        if (getSubtasks().isEmpty()) {
            return TaskStatus.NEW;
        }

        for (TaskStatus status : TaskStatus.values()) {
            boolean isAllSubtasksMatchStatus = getSubtasks()
                    .stream()
                    .allMatch(t -> t.getStatus() == status);
            if (isAllSubtasksMatchStatus) {
                this.status = status;
                return status;
            }
        }

        return TaskStatus.IN_PROGRESS;
    }
}