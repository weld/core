package org.jboss.weld.tests.invokable;

import java.util.function.Consumer;

// non-bean intentionally
public class InstanceTransformer {

    public static int runnableExecuted = 0;

    public static TransformableBean transform(TransformableBean bean){
        bean.setTransformed();
        return bean;
    }

    public static TransformableBean transform2(TransformableBean bean, Consumer<Runnable> consumer){
        consumer.accept(() -> runnableExecuted++);
        bean.setTransformed();
        return bean;
    }
}
