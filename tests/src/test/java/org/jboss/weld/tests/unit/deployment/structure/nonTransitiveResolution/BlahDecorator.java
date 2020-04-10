package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

@Decorator
public abstract class BlahDecorator implements Blah {

    @Inject
    @Delegate
    @Any
    private Blah blah;

    public void ping(int i) {
        blah.ping(i + 1);
    }

}
