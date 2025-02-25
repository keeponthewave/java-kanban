package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.exceptions.FileManagerRestoreException;
import ru.keeponthewave.tasktracker.exceptions.FileManagerSaveException;
import ru.keeponthewave.tasktracker.exceptions.TimeIntersectionException;
import ru.keeponthewave.tasktracker.model.EpicTask;
import ru.keeponthewave.tasktracker.model.SubTask;
import ru.keeponthewave.tasktracker.model.Task;
import ru.keeponthewave.tasktracker.model.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path file;

    public FileBackedTaskManager(HistoryManager historyManager, Path file) {
        super(historyManager);
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        Path filePath = file.toPath();
        var fileBackedTaskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), filePath);

        int lineNum = 1;
        int maxId = Integer.MIN_VALUE;

        try (var bufferedReader = Files.newBufferedReader(filePath)) {
            while (bufferedReader.ready()) {
                var line = bufferedReader.readLine();
                if (lineNum == 1) {
                    lineNum++;
                    continue;
                }

                TaskType taskType = TaskType.valueOf(line.split(",")[1]);

                if (taskType == TaskType.TASK) {
                    Task task = Task.fromString(line);
                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }
                    if (fileBackedTaskManager.canPrioritized(task)) {
                        if (fileBackedTaskManager.hasTimeIntersection(task, fileBackedTaskManager.prioritizedTaskSet.stream())) {
                            throw new TimeIntersectionException("На заданное время уже запланирована задача.");
                        }
                        fileBackedTaskManager.prioritizedTaskSet.add(task);
                    }
                    fileBackedTaskManager.taskMap.put(task.getId(), task);
                } else if (taskType == TaskType.EPIC) {
                    EpicTask epic = EpicTask.fromString(line);
                    if (epic.getId() > maxId) {
                        maxId = epic.getId();
                    }
                    fileBackedTaskManager.epicTaskMap.put(epic.getId(), epic);
                } else if (taskType == TaskType.SUBTASK) {
                    SubTask subTask = SubTask.fromString(line);
                    if (subTask.getId() > maxId) {
                        maxId = subTask.getId();
                    }
                    var epic = fileBackedTaskManager.epicTaskMap.get(subTask.getEpicTaskId());
                    if (epic == null) {
                        throw new FileManagerRestoreException(
                                "Ошибка при восстановлении из файла: Эпика не существует",
                                lineNum,
                                filePath);
                    }
                    if (fileBackedTaskManager.canPrioritized(subTask)) {
                        if (fileBackedTaskManager.hasTimeIntersection(subTask, fileBackedTaskManager.prioritizedTaskSet.stream())) {
                            throw new TimeIntersectionException("На заданное время уже запланирована задача.");
                        }
                        fileBackedTaskManager.prioritizedTaskSet.add(subTask);
                    }
                    fileBackedTaskManager.subTaskMap.put(subTask.getId(), subTask);
                    epic.getSubtaskIds().add(subTask.getId());
                    fileBackedTaskManager.recalculateEpicFields(epic);
                } else {
                    throw new IOException("Неверный тип задачи");
                }
                lineNum++;
            }
        } catch (IOException | ArrayIndexOutOfBoundsException | TimeIntersectionException e) {
            throw new FileManagerRestoreException(
                    "Ошибка при восстановлении из файла: " + e.getMessage(),
                    lineNum,
                    filePath);
        }
        fileBackedTaskManager.idCounter = ++maxId;
        return fileBackedTaskManager;
    }

    @Override
    public Task createTask(Task task) {
        var createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Task updateTask(Task task) {
        var updTask = super.updateTask(task);
        save();
        return updTask;
    }

    @Override
    public int deleteTaskById(int id) {
        int deletedId = super.deleteTaskById(id);
        save();
        return deletedId;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public SubTask createSubTask(SubTask task) {
        var createdTask = super.createSubTask(task);
        save();
        return createdTask;
    }

    @Override
    public SubTask updateSubTask(SubTask task) {
        var updTask = super.updateSubTask(task);
        save();
        return updTask;
    }

    @Override
    public int deleteSubTaskById(int id) {
        int deletedId = super.deleteSubTaskById(id);
        save();
        return deletedId;
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public EpicTask createEpicTask(EpicTask task) {
        var createdTask = super.createEpicTask(task);
        save();
        return createdTask;
    }

    @Override
    public EpicTask updateEpicTask(EpicTask task) {
        var updTask = super.updateEpicTask(task);
        save();
        return updTask;
    }

    @Override
    public EpicTask deleteEpicTaskById(int id) {
        EpicTask deletedTask = super.deleteEpicTaskById(id);
        save();
        return deletedTask;
    }

    @Override
    public void deleteAllEpicTasks() {
        super.deleteAllEpicTasks();
        save();
    }

    private void save() {
        try (var writer = Files.newBufferedWriter(file)) {
            writer.write(Task.SERIALIZATION_FORMAT);
            writer.newLine();

            writeTaskMap(taskMap, writer);
            writeTaskMap(epicTaskMap, writer);
            writeTaskMap(subTaskMap, writer);
        } catch (IOException e) {
            String errorMessage = "Ошибка при сохранении в файл: " + e.getMessage();
            System.out.println(errorMessage);
            throw new FileManagerSaveException(errorMessage);
        }
    }

    private void writeTaskMap(Map<Integer, ? extends Task> taskMap, BufferedWriter writer) throws IOException {
        for (var task : taskMap.values()) {
            writer.write(task.toString());
            writer.newLine();
        }
    }
}
