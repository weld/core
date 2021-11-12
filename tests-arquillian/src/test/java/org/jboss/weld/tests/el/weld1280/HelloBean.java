package org.jboss.weld.tests.el.weld1280;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

@Named
@Dependent
public class HelloBean {

    String message = "Hello from dependent scope bean";

    public String getMessage() {
        return message;
    }

}
