package org.jboss.weld.tests.autoclose.synthetic;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class AutoCloseBceCreator implements SyntheticBeanCreator<SyntheticCloseableResource> {
    @Override
    public SyntheticCloseableResource create(SyntheticInjections injections, Parameters params) {
        return new SyntheticCloseableResource("bce");
    }
}
