package org.jboss.weld.tests.invokable.async;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentBean {
    public static final AtomicInteger destroyedCounter = new AtomicInteger(0);

    public static void reset() {
        destroyedCounter.set(0);
    }

    @PreDestroy
    public void destroy() {
        destroyedCounter.incrementAndGet();
    }
}
