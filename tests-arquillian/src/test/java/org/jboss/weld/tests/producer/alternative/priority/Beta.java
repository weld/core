package org.jboss.weld.tests.producer.alternative.priority;

public class Beta {

    private String s;

    public Beta(String s) {
        this.s = s;
    }

    public String ping() {
        return s;
    }
}
