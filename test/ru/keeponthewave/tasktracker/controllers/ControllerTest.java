package ru.keeponthewave.tasktracker.controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import ru.keeponthewave.tasktracker.HttpTaskServer;
import ru.keeponthewave.tasktracker.lib.gson.adapters.DurationTypeAdapter;
import ru.keeponthewave.tasktracker.lib.gson.adapters.InstantTypeAdapter;
import ru.keeponthewave.tasktracker.managers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

abstract class ControllerTest {
    static final int PORT = 8080;
    static TaskManager manager = Managers.getDefault();
    static HttpTaskServer taskServer;
    static Gson gson;
    @BeforeAll
    public static void prepareHttpServer() throws IOException {
        var httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        var taskServer = new HttpTaskServer(httpServer);

        taskServer.configureGson(builder ->
                builder.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                        .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
        );
        
        taskServer.setControllers(
                new ArrayList<>() {{
                    add(TasksController.class);
                    add(SubtasksController.class);
                    add(EpicsController.class);
                    add(HistoryController.class);
                    add(PriorityController.class);
                }}
        );

        taskServer.configureServices(ioc -> ioc.register(TaskManager.class, manager));
        ControllerTest.taskServer = taskServer;
        gson = taskServer.getGson();
        taskServer.serve();
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpicTasks();
    }

    @AfterAll
    public static void stopServer() {
        taskServer.stop();
    }
}
