package org.jboss.weld.tests.injectionPoint;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@Dependent
public class Estate {

    @Inject
    Instance<Farm> farm;

    public Instance<Farm> getFarm() {
        return farm;
    }

}
