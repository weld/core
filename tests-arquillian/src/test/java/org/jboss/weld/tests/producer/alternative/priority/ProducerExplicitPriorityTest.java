package org.jboss.weld.tests.producer.alternative.priority;

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
public class ProducerExplicitPriorityTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProducerExplicitPriorityTest.class))
                .addPackage(ProducerExplicitPriorityTest.class.getPackage());
    }

    public static final String DEFAULT = "default";
    public static final String ALT = "alternative";
    public static final String ALT2 = "alternative2";

    @Inject
    @ProducedByMethod
    Alpha alphaMethodProducer;

    @Inject
    @ProducedByField
    Alpha alphaFieldProducer;

    @Inject
    @ProducedByMethod
    Beta betaMethodProducer;

    @Inject
    @ProducedByField
    Beta betaFieldProducer;

    @Inject
    @ProducedByMethod
    Gamma gammaMethodProducer;

    @Inject
    @ProducedByField
    Gamma gammaFieldProducer;

    @Inject
    @ProducedByMethod
    Delta deltaMethodProducer;

    @Inject
    @ProducedByField
    Delta deltaFieldProducer;

    @Test
    public void testAlternativeProducerWithPriority() {
        assertNotNull(alphaMethodProducer);
        assertNotNull(alphaFieldProducer);

        assertEquals(ALT, alphaMethodProducer.ping());
        assertEquals(ALT, alphaFieldProducer.ping());
    }

    @Test
    public void testPriorityOnProducerOverPriorityOnClass() {
        assertNotNull(betaMethodProducer);
        assertNotNull(betaFieldProducer);
        assertNotNull(gammaFieldProducer);
        assertNotNull(gammaMethodProducer);
        assertNotNull(deltaFieldProducer);
        assertNotNull(deltaMethodProducer);

        assertEquals(ALT2, betaMethodProducer.ping());
        assertEquals(ALT2, betaFieldProducer.ping());
        assertEquals(ALT2, gammaFieldProducer.ping());
        assertEquals(ALT2, gammaMethodProducer.ping());
        assertEquals(ALT2, deltaFieldProducer.ping());
        assertEquals(ALT2, deltaMethodProducer.ping());
    }
}
