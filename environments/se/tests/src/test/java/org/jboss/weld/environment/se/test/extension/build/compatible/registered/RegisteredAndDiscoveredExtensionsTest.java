package org.jboss.weld.environment.se.test.extension.build.compatible.registered;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.lite.extension.translator.LiteExtensionTranslator;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class RegisteredAndDiscoveredExtensionsTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExtensionRegisteredManuallyTest.class))
                        .addPackage(ExtensionRegisteredManuallyTest.class.getPackage())
                        .addAsServiceProvider(BuildCompatibleExtension.class, DiscoveredBce.class))
                .build();
    }

    @Test
    public void testDiscoveryAndExplicitAddition() {
        ManuallyRegisteredBce.TIMES_INVOKED = 0;
        DiscoveredBce.TIMES_INVOKED = 0;
        try (WeldContainer container = new Weld().addBuildCompatibleExtensions(ManuallyRegisteredBce.class).initialize()) {
            // assert the deployment is fine, DummyBean should be resolvable
            Assert.assertTrue(container.select(SomeBean.class).isResolvable());
            // LiteExtensionTranslator should be present
            Assert.assertTrue(container.select(LiteExtensionTranslator.class).isResolvable());
            // assert that manually added BCE was invoked correctly
            Assert.assertEquals(5, ManuallyRegisteredBce.TIMES_INVOKED);
            // assert that discovered BCE was invoked correctly
            Assert.assertEquals(5, DiscoveredBce.TIMES_INVOKED);
        }
    }

    @Test
    public void testAddingAlreadyDiscoveredExtension() {
        ManuallyRegisteredBce.TIMES_INVOKED = 0;
        DiscoveredBce.TIMES_INVOKED = 0;
        try (WeldContainer container = new Weld().addBuildCompatibleExtensions(DiscoveredBce.class, ManuallyRegisteredBce.class)
                .initialize()) {
            // assert the deployment is fine, DummyBean should be resolvable
            Assert.assertTrue(container.select(SomeBean.class).isResolvable());
            // LiteExtensionTranslator should be present
            Assert.assertTrue(container.select(LiteExtensionTranslator.class).isResolvable());
            // assert that manually added BCE was invoked correctly
            Assert.assertEquals(5, ManuallyRegisteredBce.TIMES_INVOKED);
            // despite being added manually and also discovered, it should only use a single instance of this extension
            Assert.assertEquals(5, DiscoveredBce.TIMES_INVOKED);
        }
    }
}
