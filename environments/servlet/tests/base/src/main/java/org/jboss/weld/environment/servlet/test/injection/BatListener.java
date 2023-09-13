package org.jboss.weld.environment.servlet.test.injection;

import jakarta.inject.Inject;

public abstract class BatListener {

    public static final String BAT_ATTRIBUTE_NAME = "batAttribute";

    @Inject
    Sewer sewer;

    protected boolean isSewerNameOk() {
        return sewer != null && Sewer.NAME.equals(sewer.getName());
    }
}
