package org.jboss.weld.tests.eager.synthetic;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

public class EagerSyntheticBeanBCECreator implements SyntheticBeanCreator<EagerSyntheticBeanBCE> {

    @Override
    public EagerSyntheticBeanBCE create(Instance<Object> lookup, Parameters params) {
        EagerSyntheticBeanBCE bean = new EagerSyntheticBeanBCE();
        EagerSyntheticBeanBCE.created = true;
        return bean;
    }
}
