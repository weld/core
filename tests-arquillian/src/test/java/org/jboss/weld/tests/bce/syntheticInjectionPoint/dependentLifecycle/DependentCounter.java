package org.jboss.weld.tests.bce.syntheticInjectionPoint.dependentLifecycle;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentCounter {
    public static final AtomicInteger createdCounter = new AtomicInteger(0);
    public static final AtomicInteger destroyedCounter = new AtomicInteger(0);

    private final int id;

    public DependentCounter() {
        id = createdCounter.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    @PreDestroy
    public void destroy() {
        destroyedCounter.incrementAndGet();
    }

    public static void reset() {
        createdCounter.set(0);
        destroyedCounter.set(0);
    }
}
