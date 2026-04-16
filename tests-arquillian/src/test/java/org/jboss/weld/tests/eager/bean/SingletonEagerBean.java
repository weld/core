package org.jboss.weld.tests.eager.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Eager;

/**
 * Uses {@code @SingletonStereotype} instead of {@code @Singleton}
 * directly because {@code @Singleton} is not a bean defining
 * annotation and would not be discovered in bean archives with
 * annotated discovery mode.
 */
@SingletonStereotype
@Eager
public class SingletonEagerBean {
    public static boolean constructed = false;

    @PostConstruct
    public void setup() {
        constructed = true;
    }
}
