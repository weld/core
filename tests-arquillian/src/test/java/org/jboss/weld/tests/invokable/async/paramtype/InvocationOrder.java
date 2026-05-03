package org.jboss.weld.tests.invokable.async.paramtype;

import java.util.ArrayList;
import java.util.List;

public class InvocationOrder {
    public static final List<String> events = new ArrayList<>();
    public static boolean receivedWrapped = false;

    public static void reset() {
        events.clear();
        receivedWrapped = false;
    }
}
