package org.jboss.weld.tests.producer.alternative.priority;

public class Gamma {

    private String s;

    public Gamma(String s) {
        this.s = s;
    }

    public String ping() {
        return s;
    }
}
