package org.jboss.weld.tests.autoclose.basic;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
@AutoClose
public class ThrowingAutoCloseableBean implements AutoCloseable {
    public String ping() {
        return "open";
    }

    @Override
    public void close() throws Exception {
        ActionSequence.addAction("ThrowingAutoCloseableBean.close");
        throw new RuntimeException("close failed");
    }
}
