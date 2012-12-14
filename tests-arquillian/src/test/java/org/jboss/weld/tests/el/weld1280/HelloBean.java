package org.jboss.weld.tests.el.weld1280;

import javax.inject.Named;

@Named
public class HelloBean {

    String message = "Hello from dependent scope bean";

    public String getMessage() {
        return message;
    }

}
