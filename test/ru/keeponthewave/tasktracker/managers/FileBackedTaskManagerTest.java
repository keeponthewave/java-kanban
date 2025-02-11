package ru.keeponthewave.tasktracker.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    Path file;

    @Override
    @BeforeEach
    public void prepare() {
        try {
            file = Files.createTempFile("temp_", ".csv");
        } catch (Exception e) {
            throw new RuntimeException("Error creating temporary file: " + e.getMessage());
        }

        try {
            Files.writeString(file, """
                    id,type,name,status,description,epic
                    10,EPIC,epic,DONE,It's ,epic2,
                    """);
        } catch (Exception e) {
            throw new RuntimeException("Error write epic to file: " + e.getMessage());
        }

        taskManager = FileBackedTaskManager.loadFromFile(file.toFile());
        epic = taskManager.getEpicTaskById(10);
    }

    @AfterEach
    public void removeTmpFile() {
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new RuntimeException("Error managing temporary file: " + e.getMessage());
        }
    }

    @Test
    public void shouldCorrectlySerialize() throws IOException {
        Task task = new Task("Task1", "Description task1", 1, TaskStatus.NEW);
        SubTask subTask = new SubTask("Sub Task2", "Description sub task3", 2, TaskStatus.NEW, 10);

        taskManager.createTask(task);
        taskManager.createSubTask(subTask);

        String expectedString = "id,type,name,status,description,epic\n"
                + task + "\n"
                + epic + "\n"
                + subTask + "\n";

        assertEquals(Files.readString(file), expectedString);
    }

    @Test
    public void shouldCorrectlyDeserialize() throws IOException {
        Task task = new Task("Task1", "Description task1", 1, TaskStatus.NEW);
        SubTask subTask = new SubTask("Sub Task2", "Description sub task3", 2, TaskStatus.NEW, 10);
        String serializedData = "id,type,name,status,description,epic\n"
                + task + "\n"
                + epic + "\n"
                + subTask + "\n";

        Files.writeString(file, serializedData);
        taskManager = FileBackedTaskManager.loadFromFile(file.toFile());

        assertNotNull(taskManager.getTaskById(task.getId()));
        assertNotNull(taskManager.getSubTaskById(subTask.getId()));
        assertNotNull(taskManager.getEpicTaskById(epic.getId()));
    }
}