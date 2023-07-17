package org.jboss.weld.tests.invokable.transformers.output;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.invoke.Invokable;

@Dependent
public class ActualBean {

    @Invokable
    public Beta ping(Number n) {
        return new Gamma(n, 0);
    }
}
