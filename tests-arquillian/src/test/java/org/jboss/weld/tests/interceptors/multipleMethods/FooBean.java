package org.jboss.weld.tests.interceptors.multipleMethods;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@MyBinding
public class FooBean {

    public String ping() {
        return "Foo";
    }
}
