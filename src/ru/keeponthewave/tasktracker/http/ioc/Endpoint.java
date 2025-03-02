package ru.keeponthewave.tasktracker.http.ioc;
import ru.keeponthewave.tasktracker.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Endpoint {
    HttpMethod method();
    String pattern() default "";
}
