package org.jboss.weld.tests.eager.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LazyBean {
    public static boolean constructed = false;

    @PostConstruct
    public void setup() {
        constructed = true;
    }
}
