package org.jboss.weld.tests.autoclose.interceptor;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
@AutoClose
@Monitored
public class InterceptedAutoCloseableBean implements AutoCloseable {

    public String ping() {
        return "open";
    }

    @Override
    public void close() {
        ActionSequence.addAction("InterceptedAutoCloseableBean.close");
    }
}
