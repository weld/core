package org.jboss.weld.tests.bce.syntheticInjectionPoint.dependentLifecycle;

public class SyntheticResultHolder {

    public static volatile int creatorCounterId = -1;
    public static volatile int disposerCounterId = -1;
    public static volatile int disposerDestroyedCountBeforeGet = -1;

    public static void reset() {
        creatorCounterId = -1;
        disposerCounterId = -1;
        disposerDestroyedCountBeforeGet = -1;
    }
}
