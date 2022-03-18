package org.jboss.weld.lite.extension.translator;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

class ReflectionMembers {

    private ReflectionMembers() {
    }

    static Set<Method> allMethods(Class<?> clazz) {
        Set<Method> result = new HashSet<>();
        forEachSuperclass(clazz, it -> result.addAll(Arrays.asList(it.getDeclaredMethods())));
        return result;
    }

    static Set<java.lang.reflect.Field> allFields(Class<?> clazz) {
        Set<java.lang.reflect.Field> result = new HashSet<>();
        forEachSuperclass(clazz, it -> result.addAll(Arrays.asList(it.getDeclaredFields())));
        return result;
    }

    private static void forEachSuperclass(Class<?> clazz, Consumer<Class<?>> action) {
        Queue<Class<?>> workQueue = new ArrayDeque<>();
        workQueue.add(clazz);
        while (!workQueue.isEmpty()) {
            Class<?> item = workQueue.remove();

            if (item.equals(Object.class)) {
                continue;
            }

            Class<?> superclass = item.getSuperclass();
            if (superclass != null) {
                workQueue.add(superclass);
            }
            workQueue.addAll(Arrays.asList(item.getInterfaces()));

            action.accept(item);
        }
    }
}
