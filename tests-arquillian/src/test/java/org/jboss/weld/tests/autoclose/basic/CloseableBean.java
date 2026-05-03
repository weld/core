package org.jboss.weld.tests.autoclose.basic;

import java.io.Closeable;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
@AutoClose
public class CloseableBean implements Closeable {

    @Override
    public void close() {
        ActionSequence.addAction("CloseableBean.close");
    }
}
