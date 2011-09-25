package org.jboss.weld.tests.enterprise.lifecycle;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import java.io.Serializable;

@Decorator
public abstract class AlarmedChickenHutch implements ChickenHutch, Serializable {

    private static boolean ping;

    public static void reset() {
        ping = false;
    }

    public static boolean isPing() {
        return ping;
    }

    @Inject
    @Delegate
    private ChickenHutch chickenHutch;

    public void ping() {
        ping = true;
        chickenHutch.ping();
    }

}
