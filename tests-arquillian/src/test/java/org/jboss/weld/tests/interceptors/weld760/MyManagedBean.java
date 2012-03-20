package org.jboss.weld.tests.interceptors.weld760;

import org.jboss.weld.tests.interceptors.weld760.MyObject;
import org.jboss.weld.tests.interceptors.weld760.MyStereotype;
import org.jboss.weld.tests.interceptors.weld760.MySuperClass;

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