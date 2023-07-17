package org.jboss.weld.tests.invokable.metadata.hierarchy;

public class Parent extends CommonAncestor {

    // Child class overrides this hence removing the annotation
    @TransitivelyInvokable
    public String parent() {
        return Parent.class.getSimpleName();
    }
}
