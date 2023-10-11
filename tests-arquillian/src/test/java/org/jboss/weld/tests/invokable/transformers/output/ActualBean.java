package org.jboss.weld.tests.invokable.transformers.output;

import jakarta.enterprise.context.Dependent;

@Dependent
public class ActualBean {

    public Beta ping(Number n) {
        return new Gamma(n, 0);
    }
}
