package org.jboss.weld.tests.autoclose.instance;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
@AutoClose
public class PreDestroyAutoCloseableBean implements AutoCloseable {

    @PreDestroy
    public void preDestroy() {
        ActionSequence.addAction("preDestroy");
    }

    @Override
    public void close() {
        ActionSequence.addAction("close");
    }
}
