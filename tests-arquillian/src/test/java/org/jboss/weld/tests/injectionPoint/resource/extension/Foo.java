package org.jboss.weld.tests.injectionPoint.resource.extension;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Foo {
    @Resource(name = "org.jboss.weld.tests.reproducer.Foo/world")
    private String value;

    public String value() {
        return "hello " + value;
    }
}
