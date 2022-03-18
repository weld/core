package org.jboss.weld.tests.xml.broken.clazz;

import jakarta.enterprise.inject.spi.DeploymentException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeansXmlTest {
    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(BeansXmlTest.class))
                .alternate(Bar.class)
                .beanDiscoveryMode(BeanDiscoveryMode.ALL)
                .addPackage(BeansXmlTest.class.getPackage());
    }

    @Test
    public void testClassRespectedInBeansXml() {
        //assert false; // Arquillian ShouldThrowException marks it as allowed, does not stop @Test from execution
    }

}
