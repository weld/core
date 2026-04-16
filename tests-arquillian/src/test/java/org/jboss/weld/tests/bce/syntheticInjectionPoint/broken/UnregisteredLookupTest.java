package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import static org.junit.Assert.fail;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.bce.syntheticInjectionPoint.basic.MyQualifier;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that SyntheticInjections.get() throws IllegalArgumentException
 * when looking up a type/qualifier combination that was not registered
 * via withInjectionPoint(), even if the bean exists in the archive.
 */
@RunWith(Arquillian.class)
public class UnregisteredLookupTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(UnregisteredLookupTest.class))
                .addPackage(UnregisteredLookupTest.class.getPackage())
                .addClass(MyQualifier.class)
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        UnregisteredLookupExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testUnregisteredClassLookup() {
        try {
            container.createInstance().select(UnregisteredClassResult.class).get();
            fail("Should have thrown IllegalArgumentException for unregistered Class lookup");
        } catch (IllegalArgumentException expected) {
            // expected — ExistingDefaultBean exists but was not registered
        }
    }

    @Test
    public void testUnregisteredTypeLiteralLookup() {
        try {
            container.createInstance().select(UnregisteredTypeLiteralResult.class).get();
            fail("Should have thrown IllegalArgumentException for unregistered TypeLiteral lookup");
        } catch (IllegalArgumentException expected) {
            // expected — ExistingDefaultBean exists but was not registered
        }
    }

    @Test
    public void testUnregisteredQualifiedLookup() {
        try {
            container.createInstance().select(UnregisteredQualifiedResult.class).get();
            fail("Should have thrown IllegalArgumentException for unregistered qualified lookup");
        } catch (IllegalArgumentException expected) {
            // expected — ExistingDefaultBean exists with @Default but not with @MyQualifier
        }
    }
}
