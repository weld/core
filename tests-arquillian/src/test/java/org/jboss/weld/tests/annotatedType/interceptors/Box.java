package org.jboss.weld.tests.annotatedType.interceptors;

import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

@SuppressWarnings("serial")
@Dependent
public class Box implements Serializable {

    @BoxBinding
    public boolean isIntercepted() {
        return false;
    }
}
