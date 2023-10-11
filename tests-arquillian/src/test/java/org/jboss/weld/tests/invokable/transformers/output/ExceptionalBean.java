package org.jboss.weld.tests.invokable.transformers.output;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExceptionalBean {

    public Beta ping(Integer i) {
        throw new IllegalArgumentException("expected");
    }
}
