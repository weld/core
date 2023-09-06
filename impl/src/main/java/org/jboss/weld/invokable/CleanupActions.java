package org.jboss.weld.invokable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
}
