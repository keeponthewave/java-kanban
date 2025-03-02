package ru.keeponthewave.tasktracker.controllers;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.dto.UnknownTaskDto;
import ru.keeponthewave.tasktracker.model.EpicTask;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityControllerTest extends ControllerTest {
    @Test
    public void shouldGetPrioritizedList() throws IOException, InterruptedException {
        var epic = new EpicTask("epic", "epic descr", null);
        manager.createEpicTask(epic);

        var now = Instant.now();
        var subtask = new SubTask("subtask", "subtask descr",null,
                TaskStatus.NEW, epic.getId(), now, Duration.ofMinutes(5));
        var task1 = new Task("task1", "task1 descr",null,
                TaskStatus.NEW, now.plus(Duration.ofDays(4)), Duration.ofMinutes(5));
        var task2 = new Task("task2", "task2 descr",null,
                TaskStatus.NEW, now.plus(Duration.ofDays(2)), Duration.ofMinutes(5));

        assertEquals(0, fetchPrioritizedList().size());

        manager.createSubTask(subtask);
        manager.createTask(task1);
        manager.createTask(task2);

        Task[] expectedList = new Task[] {subtask, task2, task1};

        Task[] actualList = fetchPrioritizedList().stream().map(dto -> new Task(dto.name(), dto.description(), dto.id(),
                dto.status(), dto.startTime(), dto.duration())).toArray(Task[]::new);
        assertEquals(3, fetchPrioritizedList().size());
        assertArrayEquals(expectedList, actualList);
    }

    private List<UnknownTaskDto> fetchPrioritizedList() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/prioritized", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<UnknownTaskDto>>() {}.getType());
        }
    }
}
