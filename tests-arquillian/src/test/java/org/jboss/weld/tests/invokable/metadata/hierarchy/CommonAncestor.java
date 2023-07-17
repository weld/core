package org.jboss.weld.tests.invokable.metadata.hierarchy;

// this annotation is *not* inherited
@TransitivelyInvokable
public abstract class CommonAncestor {

    public String commonAncestor() {
        return CommonAncestor.class.getSimpleName();
    }
}
