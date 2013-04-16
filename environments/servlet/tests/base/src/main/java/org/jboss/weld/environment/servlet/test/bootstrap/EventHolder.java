package org.jboss.weld.environment.servlet.test.bootstrap;

import java.util.ArrayList;
import java.util.List;

public class EventHolder {

    private EventHolder() {
    }

    public static List<Object> events = new ArrayList<Object>();

}
