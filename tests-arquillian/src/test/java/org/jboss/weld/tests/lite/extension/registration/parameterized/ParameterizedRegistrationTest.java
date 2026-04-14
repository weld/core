package org.jboss.weld.tests.lite.extension.registration.parameterized;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that @Registration types specified via TypeLiteral subclasses correctly
 * match beans and observers with parameterized types.
 *
 * The validation happens in ParameterizedRegistrationExtension - if any
 * assertion fails, deployment fails.
 */
@RunWith(Arquillian.class)
public class ParameterizedRegistrationTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(ParameterizedRegistrationTest.class))
                .addPackage(ParameterizedRegistrationTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        ParameterizedRegistrationExtension.class);
    }

    @Test
    public void testParameterizedRegistration() {
        // Validation is performed in the extension's @Validation method.
        // If deployment succeeds, all assertions in the extension passed.
    }
}
