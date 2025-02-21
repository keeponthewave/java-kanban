package ru.keeponthewave.tasktracker.model;

import java.time.Duration;
import java.time.Instant;
import java.util.InputMismatchException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task {
    protected String name;
    protected String description;
    protected Integer id;
    protected TaskStatus status;
    protected Instant startTime;
    protected Duration duration;
    protected TaskType type = TaskType.TASK;

    public final static String SERIALIZATION_FORMAT = "id,type,name,status,description,startTime,duration,epic";
    private static final Pattern DESERIALIZE_PATTERN = Pattern.compile("(?<id>\\d*),(?<type>\\w*),(?<name>.*)"
            + ",(?<status>\\w*),(?<description>.*)"
            + ",(?<startTs>\\d*),(?<durationMinutes>\\d*),(?<epicId>\\d*)"
    );

    public Task(String name, String description, Integer id, TaskStatus status, Instant startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public static Task fromString(String str) {
        TaskParams taskParams = taskParamsFromString(str);
        return new Task(taskParams.name, taskParams.description, taskParams.id, taskParams.status, taskParams.startTime,
                taskParams.duration);
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

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
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
        return String.format("%s,%s,%s,%s,%s,%s,%s,", id, type, name, status, description,
                startTime == null? "" : startTime.toEpochMilli(),
                duration == null? "" : duration.toMinutes());
    }

    public static TaskParams taskParamsFromString(String string) {
        Matcher matcher = DESERIALIZE_PATTERN.matcher(string);
        if (matcher.find()) {
            var startTs = matcher.group("startTs");
            var durationMinutes = matcher.group("durationMinutes");

            return new TaskParams(
                    Integer.parseInt(matcher.group("id")),
                    TaskType.valueOf(matcher.group("type")),
                    matcher.group("name"),
                    TaskStatus.valueOf(matcher.group("status")),
                    matcher.group("description"),
                    !matcher.group("epicId").isBlank()? Integer.parseInt(matcher.group("epicId")) : null,
                    !Objects.equals(startTs, "") ? Instant.ofEpochMilli(Long.parseLong(startTs)) : null,
                    !Objects.equals(durationMinutes, "")? Duration.ofMinutes(Long.parseLong(durationMinutes)) : null
            );
        }
        throw new InputMismatchException("Некорректный формат строки");
    }

    public record TaskParams(Integer id, TaskType type, String name, TaskStatus status, String description,
                              Integer epicId, Instant startTime, Duration duration) {
    }

    public Instant getEndTime() {
        if (duration == null || startTime == null) {
            return null;
        }

        return startTime.plus(duration);
    }


}
