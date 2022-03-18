package org.jboss.weld.tests.extensions.injection;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 *
 */
@Dependent
public class Client implements Serializable {

    @Inject
    private MyExtension myExtension;

    public MyExtension getMyExtension() {
        return myExtension;
    }
}
