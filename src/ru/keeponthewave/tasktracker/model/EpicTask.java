package ru.keeponthewave.tasktracker.model;

import ru.keeponthewave.tasktracker.exceptions.ForbiddenException;

import java.util.List;

public class EpicTask extends Task {
    private final List<SubTask> subtasks;

    public List<SubTask> getSubtasks() {
        return subtasks;
    }

    public EpicTask(
            String name,
            String description,
            Integer id,
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

    @Override
    public EpicTask updateFrom(Task other) {
        setName(other.name);
        setDescription(other.description);
        return this;
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