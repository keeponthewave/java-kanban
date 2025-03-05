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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryControllerTest extends ControllerTest {
    @Test
    public void shouldGetHistory() throws IOException, InterruptedException {
        var epic = new EpicTask("epic", "epic descr", null);
        manager.createEpicTask(epic);
        var subtask = new SubTask("subtask", "subtask descr",null,
                TaskStatus.NEW, epic.getId(), null, null);
        var task = new Task("task", "task descr",null,
                TaskStatus.NEW, null, null);
        manager.createSubTask(subtask);
        manager.createTask(task);

        assertEquals(0, fetchHistory().size());

        manager.getTaskById(task.getId());
        manager.getSubTaskById(subtask.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(subtask.getId());

        Task[] expectedList = new Task[] {task, epic, subtask};

        Task[] actualList = fetchHistory().stream().map(dto -> new Task(dto.name(), dto.description(), dto.id(),
                dto.status(), dto.startTime(), dto.duration())).toArray(Task[]::new);

        assertEquals(3, fetchHistory().size());
        assertArrayEquals(expectedList, actualList);
    }

    private List<UnknownTaskDto> fetchHistory() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create(String.format("http://localhost:%d/history", PORT));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<UnknownTaskDto>>() {}.getType());
        }
    }
}
