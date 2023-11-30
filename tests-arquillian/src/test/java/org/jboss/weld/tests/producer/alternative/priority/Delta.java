package org.jboss.weld.tests.producer.alternative.priority;

public class Delta {

    private String s;

    public Delta(String s) {
        this.s = s;
    }

    public String ping() {
        return s;
    }
}
