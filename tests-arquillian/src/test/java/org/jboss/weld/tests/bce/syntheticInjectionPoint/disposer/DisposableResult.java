package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

public class DisposableResult {
    public static boolean disposed = false;

    public static void reset() {
        disposed = false;
    }
}
