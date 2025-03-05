package ru.keeponthewave.tasktracker.controllers;

import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.dto.EpicDto;
import ru.keeponthewave.tasktracker.dto.EpicResponseDto;
import ru.keeponthewave.tasktracker.dto.SubTaskDto;
import ru.keeponthewave.tasktracker.http.HttpStatus;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubtasksControllerTest extends ControllerTest {
    static Integer epicId;

    @Test
    public void shouldAddSubtask() throws IOException, InterruptedException {
        createEpic();
        var postTask = new SubTaskDto(null,"Test", "Descr",
                TaskStatus.NEW, epicId, Instant.now() , Duration.ofMinutes(5));

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/subtasks", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpStatus.CREATED.getCode(), response.statusCode());

            Collection<SubTask> tasksFromManager = manager.getAllSubTasks();

            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals(postTask.name(), tasksFromManager.stream().toList().getFirst().getName(), "Некорректное имя задачи");
        }
    }

    @Test
    public void shouldChangeSubtask() throws IOException, InterruptedException {
        createEpic();
        var task = new SubTask("Test", "Descr",null,
                TaskStatus.NEW, epicId, Instant.now() , Duration.ofMinutes(5));
        manager.createSubTask(task);

        Collection<SubTask> tasksFromManager = manager.getAllSubTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                "Некорректный статус задачи");

        var postTask = new SubTaskDto(task.getId(),"Test", "Descr",
                TaskStatus.NEW, epicId, null, null);

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/subtasks", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpStatus.OK.getCode(), response.statusCode());

            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals(postTask.status(), tasksFromManager.stream().toList().getFirst().getStatus(),
                    "Некорректный статус задачи");
        }
    }

    @Test
    public void shouldNotAddSubtaskWhenTimeIntersectsWithTask() throws IOException, InterruptedException {
        createEpic();
        var task = new Task("Test", "Descr", null,
                TaskStatus.NEW, Instant.now() , Duration.ofMinutes(5));
        manager.createTask(task);

        Collection<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                "Некорректное имя задачи");
        var postTask = new SubTaskDto(null,"Test", "Descr",
                TaskStatus.NEW, epicId, Instant.now().plus(Duration.ofMinutes(2)) , Duration.ofMinutes(5));

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/subtasks", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpStatus.NOT_ACCEPTABLE.getCode(), response.statusCode());

            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                    "Некорректное имя задачи");
        }
    }

    @Test
    public void shouldDeleteSubtask() throws IOException, InterruptedException {
        createEpic();
        var task = new SubTask("Test", "Descr",null,
                TaskStatus.NEW, epicId, Instant.now() , Duration.ofMinutes(5));
        manager.createSubTask(task);

        Collection<SubTask> tasksFromManager = manager.getAllSubTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                "Некорректный статус задачи");

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/subtasks/%d", PORT, task.getId()));
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

    private static void createEpic() throws IOException {
        var postTask = new EpicDto(null,"Test", "Descr");

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/epics", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                epicId = gson.fromJson(response.body(), EpicResponseDto.class).id();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
