package org.jboss.weld.tests.invokable.metadata.common;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MethodLevelViaExtension {

    public void ping(){

    }

    @DefinitelyNotInvokable // should become invokable via extension
    public String pong() {
        return this.getClass().getSimpleName();
    }
}
