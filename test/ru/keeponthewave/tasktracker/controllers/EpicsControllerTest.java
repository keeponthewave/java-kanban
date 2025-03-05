package ru.keeponthewave.tasktracker.controllers;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.dto.EpicDto;
import ru.keeponthewave.tasktracker.dto.SubTaskDto;
import ru.keeponthewave.tasktracker.http.HttpStatus;
import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicsControllerTest extends ControllerTest {

    @Test
    public void shouldAddEpic() throws IOException, InterruptedException {
        var postTask = new EpicDto(null,"Test", "Descr");

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/epics", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpStatus.CREATED.getCode(), response.statusCode());

            Collection<EpicTask> tasksFromManager = manager.getAllEpicTasks();

            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals(postTask.name(), tasksFromManager.stream().toList().getFirst().getName(), "Некорректное имя задачи");
        }
    }

    @Test
    public void shouldChangeEpic() throws IOException, InterruptedException {
        var task = new EpicTask("Test", "Descr",null);
        manager.createEpicTask(task);

        Collection<EpicTask> tasksFromManager = manager.getAllEpicTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getName(), tasksFromManager.stream().toList().getFirst().getName(),
                "Некорректное имя задачи");

        var postTask = new EpicDto(task.getId(),"New name", "New descr");

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/epics", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpStatus.OK.getCode(), response.statusCode());

            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals(postTask.name(), tasksFromManager.stream().toList().getFirst().getName(),
                    "Некорректное имя задачи");
            assertEquals(postTask.description(), tasksFromManager.stream().toList().getFirst().getDescription(),
                    "Некорректное описание задачи");
        }
    }

    @Test
    public void shouldDeleteEpic() throws IOException, InterruptedException {
        var task = new EpicTask("Test", "Descr", null);
        manager.createEpicTask(task);

        Collection<EpicTask> tasksFromManager = manager.getAllEpicTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                "Некорректный статус задачи");

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/epics/%d", PORT, task.getId()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpStatus.OK.getCode(), response.statusCode());

            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
        }
    }

    @Test
    public void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        var epic = new EpicTask("Test", "Descr", null);
        manager.createEpicTask(epic);

        var subtask1 = new SubTask("subtask1", "subtask1 descr",null,
                TaskStatus.NEW, epic.getId(), null, null);
        var subtask2 = new SubTask("subtask2", "subtask2 descr",null,
                TaskStatus.NEW, epic.getId(), null, null);
        manager.createSubTask(subtask1);
        manager.createSubTask(subtask2);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/epics/%d/subtasks", PORT, epic.getId()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<SubTaskDto> resDto = gson.fromJson(response.body(), new TypeToken<List<SubTaskDto>>() {}.getType());
            List<SubTask> resList = resDto.stream().map(dto -> new SubTask(dto.name(), dto.description(), dto.id(),
                    dto.status(), dto.epicId(), dto.startTime(), dto.duration())).toList();

            assertEquals(HttpStatus.OK.getCode(), response.statusCode());
            assertTrue(resList.contains(subtask1));
            assertTrue(resList.contains(subtask2));
        }
    }
}
