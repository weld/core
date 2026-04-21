package org.jboss.weld.tests.producer.disposer.dependent;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentBean {

    private static final AtomicInteger createCount = new AtomicInteger();
    private static final AtomicInteger destroyCount = new AtomicInteger();

    public static void reset() {
        createCount.set(0);
        destroyCount.set(0);
    }

    public static int getCreateCount() {
        return createCount.get();
    }

    public static int getDestroyCount() {
        return destroyCount.get();
    }

    @PostConstruct
    public void postConstruct() {
        createCount.incrementAndGet();
    }

    @PreDestroy
    public void preDestroy() {
        destroyCount.incrementAndGet();
    }
}
