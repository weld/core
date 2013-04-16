package org.jboss.weld.environment.servlet.test.examples;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class Generator {

    private int lastInt = 0;
    private java.util.Random random = new java.util.Random(System.currentTimeMillis());

    private static final int RANGE = 100;

    java.util.Random getRandom() {
        return random;
    }

    @Produces
    @Random
    int next() {
        int nextInt = getRandom().nextInt(RANGE);
        while (nextInt == lastInt) {
            nextInt = getRandom().nextInt(RANGE);
        }
        lastInt = nextInt;
        return nextInt;
    }

}
