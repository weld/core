package org.jboss.weld.tests.invokable.metadata.common;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MethodLevelIndirectDeclaration {

    public void ping() {

    }

    @TransitivelyInvokable
    public String pong() {
        return this.getClass().getSimpleName();
    }
}
