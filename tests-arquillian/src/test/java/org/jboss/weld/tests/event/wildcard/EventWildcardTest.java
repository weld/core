package org.jboss.weld.tests.event.wildcard;

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

@RunWith(Arquillian.class)
public class EventWildcardTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EventWildcardTest.class))
                .addClass(BeanWithEventWildcard.class);
    }

    @Test
    public void testEventWithWildcard() {
        // should throw definition exception
    }
}
