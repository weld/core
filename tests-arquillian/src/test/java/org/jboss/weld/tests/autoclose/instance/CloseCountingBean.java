package org.jboss.weld.tests.autoclose.instance;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

@Dependent
@AutoClose
public class CloseCountingBean implements AutoCloseable {
    public static final AtomicInteger closeCount = new AtomicInteger(0);

    public static void reset() {
        closeCount.set(0);
    }

    @Override
    public void close() {
        closeCount.incrementAndGet();
    }
}
