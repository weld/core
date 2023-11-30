package org.jboss.weld.tests.producer.alternative.priority;

public class Alpha {

    private String s;

    public Alpha(String s) {
        this.s = s;
    }

    public String ping() {
        return s;
    }
}
