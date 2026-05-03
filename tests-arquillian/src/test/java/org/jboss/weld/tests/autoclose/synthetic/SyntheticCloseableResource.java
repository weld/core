package org.jboss.weld.tests.autoclose.synthetic;

import org.jboss.weld.test.util.ActionSequence;

public class SyntheticCloseableResource implements AutoCloseable {
    private final String name;

    public SyntheticCloseableResource(String name) {
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
