package org.jboss.weld.tests.instance.wildcard;

import static org.junit.Assert.assertNotNull;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InstanceWithWildcardTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InstanceWithWildcardTest.class))
                .addClass(BeanWithWildcardInstance.class);
    }

    @Inject
    BeanWithWildcardInstance bean;

    @Test
    public void testInstanceWithWildcard() {
        assertNotNull(bean);
        assertNotNull(bean.getWildInstance());
    }
}
