package org.jboss.weld.tests.invokable.transformers.output;

public class Alpha {

    protected Number n;

    public Alpha() {
        this.n = 0;
    }

    public String ping() {
        return n.toString();
    }
}
