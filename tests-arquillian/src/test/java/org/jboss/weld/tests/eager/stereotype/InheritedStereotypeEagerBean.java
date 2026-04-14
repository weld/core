package org.jboss.weld.tests.eager.stereotype;

import jakarta.annotation.PostConstruct;

@ChildStereotype
public class InheritedStereotypeEagerBean {
    public static boolean constructed = false;

    @PostConstruct
    public void setup() {
        constructed = true;
    }
}
