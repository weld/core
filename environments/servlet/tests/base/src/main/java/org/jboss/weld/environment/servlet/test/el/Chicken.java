package org.jboss.weld.environment.servlet.test.el;

import jakarta.inject.Named;

@Named
public class Chicken {

    public String getName() {
        return "Charlie";
    }

}
