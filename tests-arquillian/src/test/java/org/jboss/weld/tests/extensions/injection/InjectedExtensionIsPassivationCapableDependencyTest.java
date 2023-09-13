package org.jboss.weld.tests.extensions.injection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.NotSerializableException;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class InjectedExtensionIsPassivationCapableDependencyTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(InjectedExtensionIsPassivationCapableDependencyTest.class))
                .addClass(Utils.class)
                .addPackage(InjectedExtensionIsPassivationCapableDependencyTest.class.getPackage())
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Inject
    private Client client;

    @Test
    public void testInjectedExtensionIsPassivationCapableDependency() throws Exception {
        assertNotNull(client.getMyExtension());

        try {
            Utils.serialize(client);
        } catch (NotSerializableException e) {
            fail("Expected Client to be serializable, but it was not: " + e);
        }
    }
}
