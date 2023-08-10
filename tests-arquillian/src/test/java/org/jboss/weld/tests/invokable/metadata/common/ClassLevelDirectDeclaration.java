package org.jboss.weld.tests.invokable.metadata.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.invoke.Invokable;

@ApplicationScoped
@Invokable
public class ClassLevelDirectDeclaration {

    public void ping(){

    }

    public String pong() {
        return this.getClass().getSimpleName();
    }
}
