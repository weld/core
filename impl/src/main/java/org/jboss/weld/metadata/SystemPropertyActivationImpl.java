package org.jboss.weld.metadata;

import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;

public class SystemPropertyActivationImpl implements SystemPropertyActivation {

    private final String name;
    private final String value;

    public SystemPropertyActivationImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
