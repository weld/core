package org.jboss.weld.tests.invokable.metadata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.invoke.Invokable;

@ApplicationScoped
public class MethodLevelDirectDeclaration {

    public void ping(){

    }

    @Invokable
    public String pong() {
        return ClassLevelDirectDeclaration.class.getSimpleName();
    }
}
