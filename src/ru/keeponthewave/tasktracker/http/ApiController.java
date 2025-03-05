package ru.keeponthewave.tasktracker.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.keeponthewave.tasktracker.http.ioc.Controller;
import ru.keeponthewave.tasktracker.http.ioc.Endpoint;
import ru.keeponthewave.tasktracker.http.ioc.FromBody;
import ru.keeponthewave.tasktracker.http.ioc.FromPath;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ApiController implements HttpHandler {
    private final Map<String, Map<HttpMethod, Method>> routes = new HashMap<>();
    private static Gson gson;

    protected ApiController() {
        Class<?> clazz = getClass();
        if (!clazz.isAnnotationPresent(Controller.class)) {
            return;
        }
        String basePath = clazz.getAnnotation(Controller.class).path();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Endpoint.class)) {
                Endpoint endpoint = method.getAnnotation(Endpoint.class);

                String pattern = basePath
                        + endpoint.pattern();

                HttpMethod httpMethod = endpoint.method();

                Map<HttpMethod, Method> map = routes.getOrDefault(pattern, new HashMap<>());
                map.put(httpMethod, method);
                routes.put(pattern, map);
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());

        for (String pattern : routes.keySet()) {
            Pattern compiled = Pattern.compile("^"
                    + pattern.replace("{", "(?<")
                        .replace("}", ">.*)") + "$");
            Matcher matcher = compiled.matcher(path);
            if (matcher.find()) {
                var m = routes.get(pattern).get(method);
                Parameter[] inferParams = m.getParameters();
                Object[] params = new Object[inferParams.length];
                for (int i = 0; i < inferParams.length; i++) {
                    var current = inferParams[i];

                    if (current.isAnnotationPresent(FromBody.class)) {
                       String bodyStr = new String(exchange.getRequestBody().readAllBytes());
                       try {
                           var bodyInstance = gson.fromJson(bodyStr, current.getType());
                           params[i] = bodyInstance;
                           continue;
                       } catch (JsonSyntaxException e) {
                           System.out.println("Ошибка десериализации: " + e.getMessage());
                           return;
                       }
                    }

                    if (current.isAnnotationPresent(FromPath.class)) {
                        String pathParamName = current.getAnnotation(FromPath.class).name();
                        String param = matcher.group(pathParamName);
                        if (current.getType() == int.class || current.getType() == Integer.class) {
                            try {
                                params[i] = Integer.parseInt(param);
                                continue;
                            } catch (NumberFormatException e) {
                                sendJson(exchange, badRequest(
                                        String.format("Ошибка: Неверный формат параметра пути. Путь %s, где %s - целое число", pattern, pathParamName)
                                ));
                                return;
                            }
                        }
                        if (current.getType() == String.class) {
                            params[i] = param;
                            continue;
                        }
                        System.out.println("Недопустимый тип параметра в методе: " + m + current);
                    }
                }

                try {
                    HttpResult<?> result = (HttpResult<?>) m.invoke(this, params);
                    sendJson(exchange, result);
                    return;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.out.println("Ошибка обработки запроса" + e.getMessage());
                    sendJson(exchange, internalServerError("Произошла непредвиденная ошибка"));
                    return;
                }
            }
        }
        HttpResult<HttpErrorDto> notImplementedResult = notImplemented(HttpStatus.NOT_IMPLEMENTED.getTitle());
        sendJson(exchange, notImplementedResult);
    }

    protected HttpResult<HttpErrorDto> notImplemented(String message) {
        return new HttpResult<>(HttpStatus.NOT_IMPLEMENTED, new HttpErrorDto(HttpStatus.NOT_IMPLEMENTED, message));
    }

    protected HttpResult<HttpErrorDto> internalServerError(String message) {
        return new HttpResult<>(HttpStatus.INTERNAL_SERVER_ERROR, new HttpErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, message));
    }

    protected HttpResult<HttpErrorDto> notAcceptable(String message) {
        return new HttpResult<>(HttpStatus.NOT_ACCEPTABLE, new HttpErrorDto(HttpStatus.NOT_ACCEPTABLE, message));
    }

    protected HttpResult<HttpErrorDto> badRequest(String message) {
        return new HttpResult<>(HttpStatus.BAD_REQUEST, new HttpErrorDto(HttpStatus.BAD_REQUEST, message));
    }

    protected HttpResult<HttpErrorDto> notFound(String message) {
        return new HttpResult<>(HttpStatus.NOT_FOUND, new HttpErrorDto(HttpStatus.NOT_FOUND, message));
    }

    protected <T> HttpResult<T> ok(T result) {
        return new HttpResult<>(HttpStatus.OK, result);
    }

    protected <T> HttpResult<T> created(T result) {
        return new HttpResult<>(HttpStatus.CREATED, result);
    }

    private void sendJson(HttpExchange exchange, HttpResult<?> result) {
        try {
            var body = gson.toJson(result.getBody());
            byte[] resp = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(result.getStatus().getCode(), resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        } catch (Throwable e) {
            System.out.println("Ошибка при отправке: " + e.getMessage());
        }
    }
}