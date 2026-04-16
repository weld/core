package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class DummyCreator implements SyntheticBeanCreator<String> {
    @Override
    public String create(SyntheticInjections injections, Parameters params) {
        return "dummy";
    }
}
