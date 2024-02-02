package org.jboss.weld.environment.se.test.synthethic.extension;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import jakarta.enterprise.inject.se.SeContainer;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test is tailored to pass only when manually registered SE extension resides in a synthetic bean archive
 * due to per-archive alternative enablement.
 *
 * See https://issues.redhat.com/browse/WELD-2776 and the linked PR.
 */
@RunWith(Arquillian.class)
public class ExtensionInSyntheticArchiveTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExtensionInSyntheticArchiveTest.class))
                        .addPackage(ExtensionInSyntheticArchiveTest.class.getPackage()))
                .build();
    }

    @Test
    public void testAlternatives() {
        // given Foo and enabled FooAlternative alternative for Foo, injection should work similarly
        // with respect to selecting the alternative FooAlternative for Foo, in both following cases:

        // added with addBeanClass to Weld (presumably covered elsewhere serves here to compare)
        testAlternatives(weld -> weld.addBeanClass(FooInjected.class));
        // added in an extension with @Observes AfterBeanDiscovery
        testAlternatives(weld -> weld.addExtension(new AfterBeanDiscoveryAddFooInjectedExtension()));
    }

    void testAlternatives(Consumer<Weld> weldModifier) {
        Weld weld = new Weld().disableDiscovery();
        weld.addBeanClass(Foo.class);
        weld.addBeanClass(FooAlternative.class);
        weld.addAlternative(FooAlternative.class);

        weldModifier.accept(weld);

        try (SeContainer container = weld.initialize()) {
            FooInjected fooInjected = container.select(FooInjected.class).get();
            assertEquals(FooAlternative.class.getSimpleName(), fooInjected.getFoo().tellMyType());
        }
    }

}
