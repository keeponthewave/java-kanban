package ru.keeponthewave.tasktracker.controllers;

import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.dto.TaskDto;
import ru.keeponthewave.tasktracker.http.HttpStatus;
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

class TasksControllerTest extends ControllerTest {
    @Test
    public void shouldAddTask() throws IOException, InterruptedException {
        var postTask = new TaskDto(null,"Test", "Descr",
                TaskStatus.NEW, Instant.now() , Duration.ofMinutes(5));

        String taskJson = gson.toJson(postTask);


        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/tasks", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpStatus.CREATED.getCode(), response.statusCode());

            Collection<Task> tasksFromManager = manager.getAllTasks();

            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals(postTask.name(), tasksFromManager.stream().toList().getFirst().getName(), "Некорректное имя задачи");
        }
    }

    @Test
    public void shouldChangeTask() throws IOException, InterruptedException {
        var task = new Task("Test", "Descr", null,
                TaskStatus.NEW, null , null);
        manager.createTask(task);

        Collection<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                "Некорректный статус задачи");

        var postTask = new TaskDto(task.getId(),"Test", "Descr",
                TaskStatus.IN_PROGRESS, null , null);

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/tasks", PORT));
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
    public void shouldNotAddTaskWhenTimeIntersects() throws IOException, InterruptedException {
        var task = new Task("Test", "Descr", null,
                TaskStatus.NEW, Instant.now() , Duration.ofMinutes(5));
        manager.createTask(task);

        Collection<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                "Некорректное имя задачи");

        var postTask = new TaskDto(null,"Test", "Descr",
                TaskStatus.IN_PROGRESS, Instant.now().plus(Duration.ofMinutes(2)) , Duration.ofMinutes(5));

        String taskJson = gson.toJson(postTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/tasks", PORT));
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
    public void shouldDeleteTask() throws IOException, InterruptedException {
        var task = new Task("Test", "Descr", null,
                TaskStatus.NEW, Instant.now() , Duration.ofMinutes(5));
        manager.createTask(task);

        Collection<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getStatus(), tasksFromManager.stream().toList().getFirst().getStatus(),
                "Некорректный статус задачи");

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/tasks/%d", PORT, task.getId()));
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
}
