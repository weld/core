package org.jboss.weld.environment.se.test.beandiscovery;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanDiscoveryWithoutIsolationTest {
    @Deployment
    public static Archive<?> getDeployment() {
        oldArchiveIsolationProperty = System.getProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY);
        System.setProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, "false");

        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);
        JavaArchive archive = ShrinkWrap.create(BeanArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Dog.class, Cat.class, Cow.class);
        archives.add(archive);
        return archives;
    }

    private static String oldArchiveIsolationProperty;

    @AfterClass
    public static void destroy() {
        if (oldArchiveIsolationProperty == null) {
            System.clearProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY);
        } else {
            System.setProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, oldArchiveIsolationProperty);
            oldArchiveIsolationProperty = null;
        }
    }

    /**
     * Test bean discovery in SE with an empty beans XML.
     */
    @Test
    public void testBeanDiscovery(BeanManager manager) {
        assertEquals(1, manager.getBeans(Dog.class).size());
        assertEquals(1, manager.getBeans(Cat.class).size());
        assertEquals(1, manager.getBeans(Cow.class).size());
    }
}
