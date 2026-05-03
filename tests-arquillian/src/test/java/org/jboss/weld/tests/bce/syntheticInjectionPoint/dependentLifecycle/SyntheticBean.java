package org.jboss.weld.tests.bce.syntheticInjectionPoint.dependentLifecycle;

public class SyntheticBean {
    private final String value;

    public SyntheticBean(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
