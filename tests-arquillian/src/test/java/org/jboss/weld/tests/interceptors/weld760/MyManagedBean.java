package org.jboss.weld.tests.interceptors.weld760;

/**
 *
 */
@MyStereotype
public class MyManagedBean extends MySuperClass<MyObject> {

    @Override
    public MyObject perform() {
        return null;
    }
}