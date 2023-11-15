package org.jboss.weld.lite.extension.translator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

class ReflectionMembers {

    private ReflectionMembers() {
    }

    static ConcurrentMap<Class<?>, Set<java.lang.reflect.Method>> cachedMethods = new ConcurrentHashMap<>();
    static ConcurrentMap<Class<?>, Set<java.lang.reflect.Field>> cachedFields = new ConcurrentHashMap<>();

    static Set<java.lang.reflect.Method> allMethods(Class<?> clazz) {
        return cachedMethods.computeIfAbsent(clazz, ignored -> {
            Set<java.lang.reflect.Method> result = new HashSet<>();
            forEachSuperclass(clazz, it -> result.addAll(Arrays.asList(it.getDeclaredMethods())));
            return result;
        });
    }

    static Set<java.lang.reflect.Field> allFields(Class<?> clazz) {
        return cachedFields.computeIfAbsent(clazz, ignored -> {
            Set<java.lang.reflect.Field> result = new HashSet<>();
            forEachSuperclass(clazz, it -> result.addAll(Arrays.asList(it.getDeclaredFields())));
            return result;
        });
    }

    static void clearCaches() {
        cachedMethods.clear();
        cachedFields.clear();
    }

    private static void forEachSuperclass(Class<?> clazz, Consumer<Class<?>> action) {
        // an interface may be inherited multiple times, but we only want to process it once
        Set<Class<?>> alreadySeen = new HashSet<>();
        Queue<Class<?>> workQueue = new ArrayDeque<>();
        workQueue.add(clazz);
        while (!workQueue.isEmpty()) {
            Class<?> item = workQueue.remove();
            if (alreadySeen.contains(item)) {
                continue;
            }
            alreadySeen.add(item);

            Class<?> superclass = item.getSuperclass();
            if (superclass != null && !Object.class.equals(superclass)) {
                workQueue.add(superclass);
            }
            workQueue.addAll(Arrays.asList(item.getInterfaces()));

            action.accept(item);
        }
    }
}
