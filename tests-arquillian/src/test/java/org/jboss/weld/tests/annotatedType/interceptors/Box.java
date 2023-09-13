package org.jboss.weld.tests.annotatedType.interceptors;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;

@SuppressWarnings("serial")
@Dependent
public class Box implements Serializable {

    @BoxBinding
    public boolean isIntercepted() {
        return false;
    }
}
