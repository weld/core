package org.jboss.weld.tests.enterprise.lifecycle;

import java.io.Serializable;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

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
