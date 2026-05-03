package org.jboss.weld.tests.autoclose.basic;

import org.jboss.weld.test.util.ActionSequence;

public class CloseableResource implements AutoCloseable {
    private final String name;

    public CloseableResource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void close() {
        ActionSequence.addAction(name + ".close");
    }
}
