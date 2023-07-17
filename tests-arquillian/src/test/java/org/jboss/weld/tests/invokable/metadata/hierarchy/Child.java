package org.jboss.weld.tests.invokable.metadata.hierarchy;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Child extends Parent {

    @TransitivelyInvokable
    public String child() {
        return Child.class.getSimpleName();
    }

    @Override
    public String parent() {
        return Parent.class.getSimpleName() + Child.class.getSimpleName();
    }
}
