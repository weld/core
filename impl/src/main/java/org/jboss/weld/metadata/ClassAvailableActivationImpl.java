package org.jboss.weld.metadata;

import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;

public class ClassAvailableActivationImpl implements ClassAvailableActivation {

    private final String className;

    public ClassAvailableActivationImpl(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
