package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

@Baz
public class BlahImpl2 implements Blah {

    private int i;

    public int getI() {
        return i;
    }

    public void ping(int i) {
        this.i = i;
    }

}
