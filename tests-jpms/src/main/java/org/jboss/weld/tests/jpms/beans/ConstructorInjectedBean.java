package org.jboss.weld.tests.jpms.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConstructorInjectedBean {
    private final DependentBean dependent;

    @Inject
    public ConstructorInjectedBean(DependentBean dependent) {
        this.dependent = dependent;
    }

    public String delegated() {
        return dependent.value();
    }
}
