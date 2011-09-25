package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

public class Simple {

    private String id;

    public void ping(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
