package org.jboss.weld.tests.invokable;

import java.util.function.Consumer;

// non-bean class intentionally
public class ArgTransformer {

    public static String transformed = "TRANSFORMED";
    public static int runnableExecuted = 0;

    public static String transform(String s) {
        return transformed;
    }

    public static String transform2(String s, Consumer<Runnable> runnableConsumer) {
        runnableConsumer.accept(() -> runnableExecuted++);
        return transformed;
    }
}
