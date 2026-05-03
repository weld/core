package org.jboss.weld.tests.autoclose.basic;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
@AutoClose
public class ThrowingPreDestroyAutoCloseableBean implements AutoCloseable {

    @PreDestroy
    public void preDestroy() {
        ActionSequence.addAction("preDestroy.throwing");
        throw new RuntimeException("preDestroy failed");
    }

    @Override
    public void close() {
        ActionSequence.addAction("ThrowingPreDestroyAutoCloseableBean.close");
    }
}
