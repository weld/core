package org.jboss.weld.tests.lite.extension.registration.parameterized;

import jakarta.enterprise.context.Dependent;

@Dependent
public class MyStringService implements MyGenericService<String> {
    @Override
    public String hello() {
        return "hello";
    }
}
