package org.jboss.weld.invokable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.invoke.AsyncHandler;

class CleanupActions implements Consumer<Runnable> {

    private final List<Runnable> cleanupTasks = new ArrayList<>();
    private final List<Instance.Handle<?>> dependentInstances = new ArrayList<>();

    @Override
    public void accept(Runnable runnable) {
        cleanupTasks.add(runnable);
    }

    public void addInstanceHandle(Instance.Handle<?> handle) {
        if (handle.getBean().getScope().equals(Dependent.class)) {
            dependentInstances.add(handle);
        }
    }

    public void cleanup() {
        // run all registered tasks
        for (Runnable r : cleanupTasks) {
            r.run();
        }
        cleanupTasks.clear();

        // destroy dependent beans we created
        for (Instance.Handle<?> handle : dependentInstances) {
            handle.destroy();
        }
        dependentInstances.clear();
    }

    // this signature fits into the `MethodHandles.tryFinally()` combinator in case of non-`void` methods
    public static <R> R run(Throwable cause, R returnValue, CleanupActions cleanupActions) {
        cleanupActions.cleanup();
        return returnValue;
    }

    // this signature fits into the `MethodHandles.tryFinally()` combinator in case of `void` methods
    public static void run(Throwable cause, CleanupActions cleanupActions) {
        cleanupActions.cleanup();
    }

    public static <R> R runExceptionOnly(Throwable cause, R returnValue, CleanupActions cleanupActions) {
        if (cause != null) {
            cleanupActions.cleanup();
        }
        return returnValue;
    }

    public static void runExceptionOnly(Throwable cause, CleanupActions cleanupActions) {
        if (cause != null) {
            cleanupActions.cleanup();
        }
    }

    @SuppressWarnings("unchecked")
    public static <R> R applyReturnTypeHandler(R returnValue, AsyncHandler.ReturnType<Object> handler) {
        return (R) handler.transform(returnValue, () -> {
        });
    }

    @SuppressWarnings("unchecked")
    public static <R> R runWithReturnTypeHandler(Throwable cause, R returnValue,
            CleanupActions cleanupActions, AsyncHandler.ReturnType<Object> handler) {
        if (cause != null) {
            cleanupActions.cleanup();
            return returnValue;
        }
        return (R) handler.transform(returnValue, cleanupActions::cleanup);
    }
}
