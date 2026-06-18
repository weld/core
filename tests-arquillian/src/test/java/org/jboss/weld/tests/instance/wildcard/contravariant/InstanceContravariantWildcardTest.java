package org.jboss.weld.tests.instance.wildcard.contravariant;

import jakarta.enterprise.inject.spi.DefinitionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that {@code Instance<? super X>} injection points are rejected.
 * Contravariant wildcards on Instance are useless because Instance is
 * naturally covariant.
 *
 * @see <a href="https://github.com/jakartaee/cdi/issues/888">CDI #888</a>
 */
@RunWith(Arquillian.class)
public class InstanceContravariantWildcardTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(InstanceContravariantWildcardTest.class))
                .addClasses(BeanWithContravariantInstance.class, Widget.class);
    }

    @Test
    public void testContravariantInstanceWildcardRejected() {
    }
}
