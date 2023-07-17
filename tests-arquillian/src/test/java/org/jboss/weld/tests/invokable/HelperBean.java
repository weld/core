package org.jboss.weld.tests.invokable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class HelperBean {

    public static int timesStringDestroyed = 0;
    public static int timesIntDestroyed = 0;

    @Produces
    public String produceString() {
        return "bar";
    }

    @Produces
    public int produceInt() {
        return 42;
    }

    public void stringDisposer(@Disposes String s) {
        timesStringDestroyed++;
    }

    public void intDisposer(@Disposes int i) {
        timesIntDestroyed++;
    }

    public static void clearDestroyedCounters() {
        timesIntDestroyed = 0;
        timesStringDestroyed = 0;
    }
}
