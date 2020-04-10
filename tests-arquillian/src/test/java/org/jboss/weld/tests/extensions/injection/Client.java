package org.jboss.weld.tests.extensions.injection;

import java.io.Serializable;

import jakarta.inject.Inject;

/**
 *
 */
public class Client implements Serializable {

    @Inject
    private MyExtension myExtension;

    public MyExtension getMyExtension() {
        return myExtension;
    }
}
