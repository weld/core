package org.jboss.weld.tests.injectionPoint;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class Estate {

    @Inject
    Instance<Farm> farm;

    public Instance<Farm> getFarm() {
        return farm;
    }

}
