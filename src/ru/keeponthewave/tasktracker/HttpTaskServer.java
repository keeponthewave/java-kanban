package ru.keeponthewave.tasktracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.keeponthewave.tasktracker.exceptions.HttpServerInitializationError;
import ru.keeponthewave.tasktracker.http.ApiController;
import ru.keeponthewave.tasktracker.exceptions.IocException;
import ru.keeponthewave.tasktracker.http.ioc.InversionOfControlContainer;
import ru.keeponthewave.tasktracker.lib.gson.adapters.DurationTypeAdapter;
import ru.keeponthewave.tasktracker.lib.gson.adapters.InstantTypeAdapter;
import ru.keeponthewave.tasktracker.managers.*;
import ru.keeponthewave.tasktracker.http.ioc.Controller;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

public class HttpTaskServer {
    private final Logger logger = LoggerFactory.getLogger(HttpTaskServer.class);
    private Gson gson;
    private final InversionOfControlContainer ioc = new InversionOfControlContainer();

    private static final int PORT = 5050;
    private static final String HOST = "localhost";
    private final HttpServer server;

    public HttpTaskServer(HttpServer server) {
        this.server = server;
    }

    public static void main(String[] args) throws IOException {
//        var httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
//        var taskServer = new HttpTaskServer(httpServer);
//
//        taskServer.configureGson(builder ->
//                builder.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
//                        .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
//        );
//
//        taskServer.configureServices(ioc -> {
//            ioc.register(TaskManager.class, InMemoryTaskManager.class);
//            ioc.register(HistoryManager.class, InMemoryHistoryManager.class);
//        });
//        // taskServer.configureValue("backedTaskManagerPath", Path.of(""));
//
//        taskServer.serve();
    }


    public void configureGson(Function<GsonBuilder, GsonBuilder> configureFn) {
        gson = configureFn.apply(new GsonBuilder()).create();
    }

    public void configureServices(Consumer<InversionOfControlContainer> configureFn) {
        configureFn.accept(ioc);
        initControllers();
    }

    private void initControllers() {
        try {
            Reflections reflections = new Reflections(getClass().getPackageName());
            var controllers = reflections.getSubTypesOf(ApiController.class);

            Field gsonField = ApiController.class.getDeclaredField("gson");
            gsonField.setAccessible(true);
            gsonField.set(null, gson);

            for (var controllerClass : controllers) {
                if (!controllerClass.isAnnotationPresent(Controller.class)) {
                    throw new HttpServerInitializationError("Контролер должен быть помечен аннотацией " + Controller.class.getName());
                }
                String path = controllerClass.getAnnotation(Controller.class).path();
                HttpHandler handler = (HttpHandler) ioc.resolve(controllerClass);
                server.createContext(path, handler);
            }

        } catch (IocException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("Ошибка инициализации контроллера", e);
            throw new HttpServerInitializationError("Ошибка инициализации контроллера", e);
        }
    }

    public void serve() {
        server.start();
        logger.info("Started on http://{}:{}", HOST, PORT);
    }

    public void stop() {
        server.stop(0);
    }

    public Gson getGson() {
        return gson;
    }
}
