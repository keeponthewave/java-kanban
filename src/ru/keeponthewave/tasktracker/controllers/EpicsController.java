package ru.keeponthewave.tasktracker.controllers;

import ru.keeponthewave.tasktracker.dto.EpicDto;
import ru.keeponthewave.tasktracker.dto.EpicResponseDto;
import ru.keeponthewave.tasktracker.dto.SubTaskDto;
import ru.keeponthewave.tasktracker.http.ApiController;
import ru.keeponthewave.tasktracker.http.HttpMethod;
import ru.keeponthewave.tasktracker.http.HttpResult;
import ru.keeponthewave.tasktracker.http.ioc.*;
import ru.keeponthewave.tasktracker.managers.TaskManager;
import ru.keeponthewave.tasktracker.model.EpicTask;

import java.util.NoSuchElementException;

@Controller(path = "/epics")
public class EpicsController extends ApiController {
    private final TaskManager manager;

    @InjectableConstructor
    public EpicsController(TaskManager manager) {
        this.manager = manager;
    }

    @Endpoint(method = HttpMethod.GET)
    public HttpResult<?> getAllEpics() {
        return Ok(manager.getAllEpicTasks()
                .stream()
                .map(task -> new EpicResponseDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                        task.getSubtaskIds(), task.getStartTime(), task.getDuration()

                )).toList());
    }

    @Endpoint(method = HttpMethod.GET, pattern = "/{id}")
    public HttpResult<?> getEpicById(@FromPath(name = "id") int id) {
        try {
            var task = manager.getEpicTaskById(id);
            return Ok(new EpicResponseDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                    task.getSubtaskIds(), task.getStartTime(), task.getDuration()
            ));
        } catch (NoSuchElementException e) {
            return NotFound(String.format("Подзадачи c id=%d не существует", id));
        }
    }

    @Endpoint(method = HttpMethod.POST)
    public HttpResult<?> addEpicTask(@FromBody EpicDto dto) {
        try {
            var task = new EpicTask(dto.name(), dto.description(), dto.id());
            if (task.getId() == null) {
                EpicTask created = manager.createEpicTask(task);
                return Created(
                        new EpicResponseDto(created.getId(), created.getName(), created.getDescription(),
                                created.getStatus(), created.getSubtaskIds(),
                                created.getStartTime(), created.getDuration())
                );
            } else {
                EpicTask updated = manager.updateEpicTask(task);
                return Ok(
                        new EpicResponseDto(updated.getId(), updated.getName(), updated.getDescription(),
                                updated.getStatus(), updated.getSubtaskIds(),
                                updated.getStartTime(), updated.getDuration())
                );
            }
        } catch (NoSuchElementException e) {
            return NotFound(e.getMessage());
        }
    }

    @Endpoint(method = HttpMethod.DELETE, pattern = "/{id}")
    public HttpResult<?> deleteEpicTask(@FromPath(name = "id") int id) {
        try {
            EpicTask task = manager.deleteEpicTaskById(id);
            return Ok(
                    new EpicResponseDto(task.getId(), task.getName(), task.getDescription(),
                            task.getStatus(), task.getSubtaskIds(),
                            task.getStartTime(), task.getDuration())
            );
        } catch (NoSuchElementException e) {
            return NotFound(e.getMessage());
        }
    }

    @Endpoint(method = HttpMethod.GET, pattern = "/{id}/subtasks")
    public HttpResult<?> getEpicsSubtasks(@FromPath(name = "id") int id) {
        try {
            EpicTask epic = manager.getEpicTaskById(id);
            return Ok(
                    manager.getEpicSubTasks(epic)
                            .stream()
                            .map(task -> new SubTaskDto(task.getId(), task.getName(), task.getDescription(), task.getStatus(),
                                    task.getEpicTaskId(), task.getStartTime(), task.getDuration()

                            )).toList()
            );
        } catch (NoSuchElementException e) {
            return NotFound(e.getMessage());
        }
    }
}
