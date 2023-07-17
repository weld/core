package org.jboss.weld.tests.invokable.transformers.input;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.invoke.Invokable;

@Dependent
public class ActualBean {

    private Integer number;

    public ActualBean() {
        this(0);
    }

    public ActualBean(Integer i) {
        this.number = i;
    }

    public Integer getNumber() {
        return number;
    }

    @Invokable
    public Beta ping(Number n) {
        return new Gamma(n, number);
    }
}
