package ru.keeponthewave.tasktracker.managers;

import org.junit.jupiter.api.BeforeEach;
import ru.keeponthewave.tasktracker.model.EpicTask;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    @BeforeEach
    public void prepare() {
        taskManager = (InMemoryTaskManager) Managers.getDefault();
        epic = new EpicTask("epic", "It's ", 10);
        taskManager.createEpicTask(epic);
    }
}