package ru.keeponthewave.tasktracker.controllers;

import ru.keeponthewave.tasktracker.dto.UnknownTaskDto;
import ru.keeponthewave.tasktracker.http.ApiController;
import ru.keeponthewave.tasktracker.http.HttpMethod;
import ru.keeponthewave.tasktracker.http.HttpResult;
import ru.keeponthewave.tasktracker.http.ioc.Controller;
import ru.keeponthewave.tasktracker.http.ioc.Endpoint;
import ru.keeponthewave.tasktracker.http.ioc.InjectableConstructor;
import ru.keeponthewave.tasktracker.managers.TaskManager;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.TaskType;

@Controller(path = "/prioritized")
public class PriorityController extends ApiController {
    private final TaskManager manager;

    @InjectableConstructor
    public PriorityController(TaskManager manager) {
        this.manager = manager;
    }

    @Endpoint(method = HttpMethod.GET)
    public HttpResult<?> getPrioritized() {
        return ok(manager.getPrioritizedTasks().stream().map(t -> {
            if (t.getType() == TaskType.SUBTASK) {
                var subtask = (SubTask) t;
                return new UnknownTaskDto(subtask.getId(), subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                        null, t.getStartTime(), t.getDuration(), subtask.getEpicTaskId(), t.getType());
            }
            return new UnknownTaskDto(t.getId(), t.getName(), t.getDescription(), t.getStatus(),
                    null, t.getStartTime(), t.getDuration(), null, t.getType());
        }).toList());
    }
}
