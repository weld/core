package org.jboss.weld.tests.producer.alternative.priority.complex;

public class Bar {
    private String s;

    public Bar(String s) {
        this.s = s;
    }

    public String ping() {
        return s;
    }
}
