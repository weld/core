package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

@Basic
public class Complex {

    private String id;

    public void ping(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
