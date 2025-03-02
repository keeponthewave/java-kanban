package ru.keeponthewave.tasktracker.controllers;

import ru.keeponthewave.tasktracker.dto.SubTaskDto;
import ru.keeponthewave.tasktracker.exceptions.TimeIntersectionException;
import ru.keeponthewave.tasktracker.http.ApiController;
import ru.keeponthewave.tasktracker.http.HttpMethod;
import ru.keeponthewave.tasktracker.http.HttpResult;
import ru.keeponthewave.tasktracker.http.ioc.*;
import ru.keeponthewave.tasktracker.managers.TaskManager;
import ru.keeponthewave.tasktracker.model.SubTask;

import java.util.NoSuchElementException;

@Controller(path = "/subtasks")
public class SubtasksController extends ApiController {
    private final TaskManager manager;

    @InjectableConstructor
    public SubtasksController(TaskManager manager) {
        this.manager = manager;
    }

    @Endpoint(method = HttpMethod.GET)
    public HttpResult<?> getAllTasks() {
        return ok(manager.getAllSubTasks()
                .stream()
                .map(task -> new SubTaskDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                        task.getEpicTaskId(), task.getStartTime(), task.getDuration()

                )).toList());
    }

    @Endpoint(method = HttpMethod.GET, pattern = "/{id}")
    public HttpResult<?> getSubtaskById(@FromPath(name = "id") int id) {
        try {
            var task = manager.getSubTaskById(id);
            return ok(new SubTaskDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                    task.getEpicTaskId(), task.getStartTime(), task.getDuration()
            ));
        } catch (NoSuchElementException e) {
            return notFound(String.format("Подзадачи c id=%d не существует", id));
        }
    }

    @Endpoint(method = HttpMethod.POST)
    public HttpResult<?> addSubtask(@FromBody SubTaskDto dto) {
        try {
            var task = new SubTask(dto.name(), dto.description(), dto.id(), dto.status(), dto.epicId(), dto.startTime(), dto.duration());
            if (task.getId() == null) {
                SubTask created = manager.createSubTask(task);
                return Created(
                        new SubTaskDto(created.getId(), created.getName(), created.getDescription(),
                                created.getStatus(), created.getEpicTaskId(), created.getStartTime(),
                                created.getDuration())
                );
            } else {
                SubTask updated = manager.updateSubTask(task);
                return ok(
                        new SubTaskDto(updated.getId(), updated.getName(), updated.getDescription(), updated.getStatus(),
                                updated.getEpicTaskId(), updated.getStartTime(), updated.getDuration())
                );
            }
        } catch (TimeIntersectionException e) {
            return notAcceptable(e.getMessage());
        } catch (NoSuchElementException e) {
            return notFound(e.getMessage());
        }
    }

    @Endpoint(method = HttpMethod.DELETE, pattern = "/{id}")
    public HttpResult<?> deleteSubtask(@FromPath(name = "id") int id) {
        try {
            SubTask task = manager.deleteSubTaskById(id);
            return ok(
                    new SubTaskDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                            task.getEpicTaskId(), task.getStartTime(), task.getDuration())
            );
        } catch (NoSuchElementException e) {
            return notFound(e.getMessage());
        }
    }
}
