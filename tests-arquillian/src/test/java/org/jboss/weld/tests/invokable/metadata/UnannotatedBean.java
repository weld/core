package org.jboss.weld.tests.invokable.metadata;

import jakarta.enterprise.context.ApplicationScoped;

// @TransitivelyInvokable is added programmatically
@ApplicationScoped
public class UnannotatedBean {

    public void ping(){

    }

    @TransitivelyInvokable
    public String pong(int i) {
        return ClassLevelDirectDeclaration.class.getSimpleName();
    }
}
