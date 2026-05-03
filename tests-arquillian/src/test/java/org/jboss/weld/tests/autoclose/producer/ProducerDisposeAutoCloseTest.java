package org.jboss.weld.tests.autoclose.producer;

import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that {@link Producer#dispose(Object)} calls {@code close()} on auto-closeable
 * producer bean instances, as required by the CDI 5.0 spec's {@code Producer.dispose()} contract.
 */
@RunWith(Arquillian.class)
public class ProducerDisposeAutoCloseTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(ProducerDisposeAutoCloseTest.class))
                .addPackage(ProducerDisposeAutoCloseTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ProducerCaptureExtension.class);
    }

    @Inject
    BeanManager beanManager;

    @Test
    public void testProducerDisposeCallsClose() {
        Producer<CloseableResource> producer = ProducerCaptureExtension.getCapturedProducer();
        assertNotNull("Extension should have captured the producer", producer);

        ActionSequence.reset();
        CreationalContext<CloseableResource> cc = beanManager.createCreationalContext(null);
        CloseableResource instance = producer.produce(cc);
        assertNotNull(instance);

        producer.dispose(instance);

        ActionSequence.assertSequenceDataEquals("disposer", "produced.close");
    }

    @Test
    public void testBeanDestroyCallsClose() {
        ActionSequence.reset();
        createAndDestroy(CloseableResource.class, AutoCloseProducerQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("disposer", "produced.close");
    }

    @Test
    public void testDependentObjectsDestroyedAfterDisposerAndClose() {
        ActionSequence.reset();
        DependentHelper.reset();
        createAndDestroy(CloseableResource.class, DependentParamQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("disposer", "withDependentParam.close", "DependentHelper.preDestroy");
    }

    @SuppressWarnings("unchecked")
    private <T> void createAndDestroy(Class<T> type, Annotation... qualifiers) {
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type, qualifiers));
        CreationalContext<T> cc = beanManager.createCreationalContext(bean);
        T instance = bean.create(cc);
        bean.destroy(instance, cc);
    }
}
