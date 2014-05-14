package org.jboss.weld.tests.interceptors.bridgemethods.hierarchy;

import org.jboss.weld.test.util.ActionSequence;

@Fast
public class Child extends Parent<String> {

    public void invoke(String param) {
        ActionSequence.addAction(Child.class.getName());
    }

}
