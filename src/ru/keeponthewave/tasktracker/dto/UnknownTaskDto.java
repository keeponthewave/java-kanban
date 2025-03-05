package ru.keeponthewave.tasktracker.dto;

import ru.keeponthewave.tasktracker.model.TaskStatus;
import ru.keeponthewave.tasktracker.model.TaskType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record UnknownTaskDto(Integer id, String name, String description, TaskStatus status, List<Integer> SubTaskIds, Instant startTime, Duration duration, Integer epicId, TaskType type) {

}