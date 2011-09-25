package org.jboss.weld.environment.servlet.test.el;

import javax.inject.Named;

@Named
public class Chicken {

    public String getName() {
        return "Charlie";
    }

}
