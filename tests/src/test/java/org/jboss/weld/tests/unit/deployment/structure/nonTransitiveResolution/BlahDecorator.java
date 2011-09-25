package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

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
