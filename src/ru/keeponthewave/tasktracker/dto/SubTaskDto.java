package ru.keeponthewave.tasktracker.dto;

import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.time.Duration;
import java.time.Instant;

public record SubTaskDto(Integer id, String name, String description, TaskStatus status, Integer epicId, Instant startTime, Duration duration) {

}
