package org.jboss.weld.tests.eager.stereotype;

import jakarta.annotation.PostConstruct;

@EagerStereotype
public class StereotypeEagerBean {
    public static boolean constructed = false;

    @PostConstruct
    public void setup() {
        constructed = true;
    }
}
