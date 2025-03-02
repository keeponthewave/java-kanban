package ru.keeponthewave.tasktracker.controllers;

import ru.keeponthewave.tasktracker.dto.UnknownTaskDto;
import ru.keeponthewave.tasktracker.http.ApiController;
import ru.keeponthewave.tasktracker.http.HttpMethod;
import ru.keeponthewave.tasktracker.http.HttpResult;
import ru.keeponthewave.tasktracker.http.ioc.*;
import ru.keeponthewave.tasktracker.managers.TaskManager;
import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.TaskType;

@Controller(path = "/history")
public class HistoryController extends ApiController {
    private final TaskManager manager;

    @InjectableConstructor
    public HistoryController(TaskManager manager) {
        this.manager = manager;
    }

    @Endpoint(method = HttpMethod.GET)
    public HttpResult<?> getHistory() {
        return Ok(manager.getHistory().stream().map(t -> {
            if (t.getType() == TaskType.SUBTASK) {
                var subtask = (SubTask) t;
                return new UnknownTaskDto(subtask.getId(), subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                        null, t.getStartTime(), t.getDuration(), subtask.getEpicTaskId(), t.getType());
            }
            if (t.getType() == TaskType.EPIC) {
                var epic = (EpicTask) t;
                return new UnknownTaskDto(epic.getId(), epic.getName(), epic.getDescription(), epic.getStatus(),
                        epic.getSubtaskIds(), epic.getStartTime(), epic.getDuration(), null, epic.getType());
            }
            return new UnknownTaskDto(t.getId(), t.getName(), t.getDescription(), t.getStatus(),
                    null, t.getStartTime(), t.getDuration(), null, t.getType());
        }).toList());
    }
}
