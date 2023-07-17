package org.jboss.weld.tests.invokable.transformers.output;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.invoke.Invokable;

@ApplicationScoped
public class ExceptionalBean {

    @Invokable
    public Beta ping(Integer i) {
        throw new IllegalArgumentException("expected");
    }
}
