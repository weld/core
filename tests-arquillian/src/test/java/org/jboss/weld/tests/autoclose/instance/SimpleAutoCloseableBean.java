package org.jboss.weld.tests.autoclose.instance;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

@Dependent
@AutoClose
public class SimpleAutoCloseableBean implements AutoCloseable {
    static boolean closed = false;

    public static void reset() {
        closed = false;
    }

    @Override
    public void close() {
        closed = true;
    }
}
