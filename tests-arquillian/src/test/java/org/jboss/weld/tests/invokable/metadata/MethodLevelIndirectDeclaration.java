package org.jboss.weld.tests.invokable.metadata;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MethodLevelIndirectDeclaration {

    public void ping(){

    }

    @TransitivelyInvokable
    public String pong() {
        return ClassLevelDirectDeclaration.class.getSimpleName();
    }
}
