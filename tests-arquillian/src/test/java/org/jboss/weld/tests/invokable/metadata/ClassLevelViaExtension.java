package org.jboss.weld.tests.invokable.metadata;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@DefinitelyNotInvokable // should become @Invokable via extension
public class ClassLevelViaExtension {

    public void ping(){

    }

    public String pong() {
        return ClassLevelDirectDeclaration.class.getSimpleName();
    }
}
