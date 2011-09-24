package org.jboss.weld.tests.producer.method;

public class Food {

    private final String type;

    private boolean made;

    public Food(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void make() {
        this.made = true;
    }

    public boolean isMade() {
        return made;
    }

}
