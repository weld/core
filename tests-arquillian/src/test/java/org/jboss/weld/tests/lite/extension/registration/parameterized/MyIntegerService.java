package org.jboss.weld.tests.lite.extension.registration.parameterized;

import jakarta.enterprise.context.Dependent;

@Dependent
public class MyIntegerService implements MyGenericService<Integer> {
    @Override
    public Integer hello() {
        return 42;
    }
}
