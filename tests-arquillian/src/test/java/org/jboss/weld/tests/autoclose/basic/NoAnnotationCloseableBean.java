package org.jboss.weld.tests.autoclose.basic;

import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
public class NoAnnotationCloseableBean implements AutoCloseable {
    public String ping() {
        return "open";
    }

    @Override
    public void close() {
        ActionSequence.addAction("NoAnnotationCloseableBean.close");
    }
}
