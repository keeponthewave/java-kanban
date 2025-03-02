package ru.keeponthewave.tasktracker.controllers;

import ru.keeponthewave.tasktracker.dto.TaskDto;
import ru.keeponthewave.tasktracker.exceptions.TimeIntersectionException;
import ru.keeponthewave.tasktracker.http.*;
import ru.keeponthewave.tasktracker.http.ioc.*;
import ru.keeponthewave.tasktracker.managers.TaskManager;
import ru.keeponthewave.tasktracker.model.Task;

import java.util.NoSuchElementException;

@Controller(path = "/tasks")
public class TasksController extends ApiController {
    private final TaskManager manager;

    @InjectableConstructor
    public TasksController(TaskManager manager) {
        this.manager = manager;
    }

    @Endpoint(method = HttpMethod.GET)
    public HttpResult<?> getAllTasks() {
        return ok(manager.getAllTasks()
                .stream()
                .map(task -> new TaskDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                        task.getStartTime(), task.getDuration()

                )).toList());
    }

    @Endpoint(method = HttpMethod.GET, pattern = "/{id}")
    public HttpResult<?> getTaskById(@FromPath(name = "id") int id) {
        try {
            var task = manager.getTaskById(id);
            return ok(new TaskDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                    task.getStartTime(), task.getDuration()
            ));
        } catch (NoSuchElementException e) {
            return notFound(e.getMessage());
        }
    }

    @Endpoint(method = HttpMethod.POST)
    public HttpResult<?> addTask(@FromBody TaskDto dto) {
        try {
            var task = new Task(dto.name(), dto.description(), dto.id(), dto.status(), dto.startTime(), dto.duration());
            if (task.getId() == null) {
                Task created = manager.createTask(task);
                return created(
                        new TaskDto(created.getId(), created.getName(), created.getDescription(), created.getStatus(),
                                created.getStartTime(), created.getDuration())
                );
            } else {
                Task updated = manager.updateTask(task);
                return ok(
                        new TaskDto(updated.getId(), updated.getName(), updated.getDescription(), updated.getStatus(),
                                updated.getStartTime(), updated.getDuration())
                );
            }
        } catch (TimeIntersectionException e) {
            return notAcceptable(e.getMessage());
        } catch (NoSuchElementException e) {
            return notFound(e.getMessage());
        }
    }

    @Endpoint(method = HttpMethod.DELETE, pattern = "/{id}")
    public HttpResult<?> deleteTask(@FromPath(name = "id") int id) {
        try {
            Task task = manager.deleteTaskById(id);
            return ok(
                    new TaskDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                            task.getStartTime(), task.getDuration())
            );
        } catch (NoSuchElementException e) {
            return notFound(e.getMessage());
        }
    }
}
