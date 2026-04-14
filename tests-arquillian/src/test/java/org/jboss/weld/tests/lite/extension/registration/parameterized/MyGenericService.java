package org.jboss.weld.tests.lite.extension.registration.parameterized;

public interface MyGenericService<T> {
    T hello();
}
