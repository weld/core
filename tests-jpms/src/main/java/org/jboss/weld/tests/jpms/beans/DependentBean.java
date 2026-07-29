package org.jboss.weld.tests.jpms.beans;

import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentBean {
    public String value() {
        return "dependent";
    }
}
