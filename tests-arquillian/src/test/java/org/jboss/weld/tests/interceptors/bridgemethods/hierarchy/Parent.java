package org.jboss.weld.tests.interceptors.bridgemethods.hierarchy;

import org.jboss.weld.test.util.ActionSequence;

@Fast
public class Parent<T> {

    public void invoke(T param) {
        ActionSequence.addAction(Parent.class.getName());
    }

}
