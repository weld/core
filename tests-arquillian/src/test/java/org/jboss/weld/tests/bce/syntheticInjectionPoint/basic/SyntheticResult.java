package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

/**
 * Simple holder for the result produced by the synthetic bean's creator.
 */
public class SyntheticResult {
    private String value;

    public SyntheticResult() {
    }

    public SyntheticResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
