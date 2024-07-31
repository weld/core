package org.jboss.weld.tests.accessibility.bce.lib;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

import org.jboss.weld.tests.accessibility.bce.MyBean;

public class MyBeanCreator implements SyntheticBeanCreator<SomeType> {
    @Override
    public SomeType create(Instance<Object> lookup, Parameters params) {
        // assert that Instance provided here (BCE in non bean archive lib) can "see" MyBean
        assertTrue(lookup.select(MyBean.class).isResolvable());
        return new SomeType();
    }
}
