package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentHelper {
    public static final AtomicInteger destroyedCounter = new AtomicInteger(0);

    public static void reset() {
        destroyedCounter.set(0);
    }

    public String ping() {
        return "dependent";
    }

    @PreDestroy
    public void destroy() {
        destroyedCounter.incrementAndGet();
    }
}
