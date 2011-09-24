package org.jboss.weld.tests.enterprise.lifecycle;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;

@Stateful
public class StandardChickenHutch implements ChickenHutch {

    private static boolean preDestroy;

    private static boolean ping;

    public static boolean isPing() {
        return ping;
    }

    public static void reset() {
        preDestroy = false;
        ping = false;
    }

    public static boolean isPredestroy() {
        return preDestroy;
    }

    public void ping() {
        ping = true;
    }

    @PreDestroy
    public void preDestroy() {
        if (ping) {
            preDestroy = true;
        } else {
            preDestroy = false;
        }
    }
}
