package ru.keeponthewave.tasktracker.core.abstractive;

import ru.keeponthewave.tasktracker.core.model.EpicTask;
import ru.keeponthewave.tasktracker.core.model.SubTask;

import java.util.Collection;

public interface EpicSpecificManager extends TaskManager<EpicTask> {
    Collection<SubTask> getSubTasks(EpicTask task);
}