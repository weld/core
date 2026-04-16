package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DisposerHelper {
    public void cleanup() {
        // simulate cleanup work
    }
}
