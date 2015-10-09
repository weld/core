package org.jboss.weld.tests.injectionPoint.weld1950;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class Weld1950InjectionPointTest {

    @Inject
    ModelBean modelBean;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(Weld1950InjectionPointTest.class)).addPackage(Weld1950InjectionPointTest.class.getPackage());
    }

    @Test
    public void testInjectionPointGetBean(UserProducer producer) {
        modelBean.ping();
        Assert.assertEquals(TestDependentBean.class, producer.getIp().getBean().getBeanClass());
    }
}
