package ru.keeponthewave.tasktracker.http.ioc;

import ru.keeponthewave.tasktracker.exceptions.IocException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InversionOfControlContainer {
    private final Map<Class<?>, Class<?>> classProvider = new HashMap<>();
    private final Map<Class<?>, Object> implementationProvider = new HashMap<>();
    private final Map<String, Object> valueProvider = new HashMap<>();

    public void register(Class<?> forClass, Class<?> injectable) {
        implementationProvider.remove(forClass);
        classProvider.put(forClass, injectable);
    }

    public void register(Class<?> forClass, Object implementation) {
        classProvider.remove(forClass);
        implementationProvider.put(forClass, implementation);
    }

    public void register(String token, Object value) {
        valueProvider.put(token, value);
    }

    public Object resolve(Class<?> clazz) {
        Optional<Constructor<?>> constructorOpt = Arrays
                .stream(clazz.getDeclaredConstructors())
                .filter(ctr -> ctr.isAnnotationPresent(InjectableConstructor.class))
                .findFirst();
        constructorOpt.orElseThrow(() -> new IocException(clazz.getName() + " - не имеет конструктора для инъекции зависимостей"));
        var constructor = constructorOpt.get();

        Parameter[] inferParams = constructor.getParameters();
        Object[] params = new Object[inferParams.length];

        for (int i = 0; i < inferParams.length; i++) {
            var inferParam = inferParams[i];
            if (inferParam.isAnnotationPresent(InjectValue.class)) {
                String token = inferParam.getAnnotation(InjectValue.class).token();
                var value = valueProvider.get(token);
                if (value == null) {
                    throw new IocException("Невозмножно внедрить значение: " + token);
                }
                params[i] = value;
                continue;
            }
            Class<?> type = inferParam.getType();

            Object impl = implementationProvider.get(type);

            if (impl != null) {
                params[i] = impl;
                continue;
            }

            Class<?> injClass = classProvider.get(type);

            if (injClass != null) {
                params[i] = resolve(injClass);
                implementationProvider.put(type, params[i]);
                continue;
            }

            throw new IocException("Зависимость не зарегистрирована в контейнере: " + type.getName());
        }
        try {
            return constructor.newInstance(params);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IocException("Ошибка при инициализации объекта класса: " + clazz.getName());
        }
    }
}
