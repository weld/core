package org.jboss.weld.environment.se.test.synthethic.extension;

import jakarta.inject.Inject;

public class FooInjected {
    @Inject
    Foo foo;

    Foo getFoo() {
        return foo;
    }
}
