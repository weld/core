package org.jboss.weld.tests.invokable.metadata;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@TransitivelyInvokable
public class ClassLevelIndirectDeclaration {

    public void ping(){

    }

    public String pong() {
        return ClassLevelDirectDeclaration.class.getSimpleName();
    }
}
