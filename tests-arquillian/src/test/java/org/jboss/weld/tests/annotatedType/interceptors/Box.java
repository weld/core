package org.jboss.weld.tests.annotatedType.interceptors;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Box implements Serializable {

    @BoxBinding
    public boolean isIntercepted() {
        return false;
    }
}
