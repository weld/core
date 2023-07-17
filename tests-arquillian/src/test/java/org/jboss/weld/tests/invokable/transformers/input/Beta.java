package org.jboss.weld.tests.invokable.transformers.input;

public class Beta extends Alpha {

    protected Integer i;

    public Beta() {
        super();
        this.i = 0;
    }

    public Beta(Number n, Integer i) {
        this.n = n;
        this.i = i;
    }

    public Integer getInteger() {
        return i;
    }
}
