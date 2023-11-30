package org.jboss.weld.tests.producer.alternative.priority.complex;

import static org.junit.Assert.assertEquals;
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
public class ProducerOnLocallyEnabledAltTest {

    public static final String DEFAULT = "default";
    public static final String ALT = "alternative";
    public static final String ALT2 = "alternative2";

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProducerOnLocallyEnabledAltTest.class))
                .alternate(LocallyEnabledAlternative.class)
                .addPackage(ProducerOnLocallyEnabledAltTest.class.getPackage());
    }

    @Inject
    Bar bar;

    @Inject
    Foo foo;

    @Test
    public void testLocallyEnabledAlternativeHasHighestPrio() {
        assertNotNull(bar);
        assertNotNull(foo);

        assertEquals(ALT2, foo.ping());
        assertEquals(ALT2, bar.ping());
    }
}
