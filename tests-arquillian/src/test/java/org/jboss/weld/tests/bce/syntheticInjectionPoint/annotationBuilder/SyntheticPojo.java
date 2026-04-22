package org.jboss.weld.tests.bce.syntheticInjectionPoint.annotationBuilder;

public class SyntheticPojo {
    public final String plainName;
    public final String specialName;
    public final String taggedName;

    public SyntheticPojo(String plainName, String specialName, String taggedName) {
        this.plainName = plainName;
        this.specialName = specialName;
        this.taggedName = taggedName;
    }
}
