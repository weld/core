package org.jboss.weld.tests.interceptors.weld1019;

import java.io.Serializable;

/**
 *
 */
@MyScope
public class HelloBean implements Serializable {
    private static final long serialVersionUID = -3216074155876250969L;

    @UpperCased
    public String getMessage() {
        return "Hello World";
    }
}
