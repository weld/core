package org.jboss.weld.metadata;

import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;

public class ClassAvailableActivationImpl implements ClassAvailableActivation {

    private final String className;
    private final boolean inverted;

    public ClassAvailableActivationImpl(String className, boolean inverted) {
        this.className = className;
        this.inverted = inverted;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean isInverted() {
        return inverted;
    }

}
