package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

public class BlahImpl implements Blah {

    private int i;

    public int getI() {
        return i;
    }

    public void ping(int i) {
        this.i = i;
    }

}
