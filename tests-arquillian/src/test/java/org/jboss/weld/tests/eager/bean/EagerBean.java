package org.jboss.weld.tests.eager.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Eager;

@ApplicationScoped
@Eager
public class EagerBean {
    public static boolean constructed = false;

    @PostConstruct
    public void setup() {
        constructed = true;
    }
}
