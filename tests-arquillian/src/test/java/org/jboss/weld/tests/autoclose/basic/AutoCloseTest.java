package org.jboss.weld.tests.autoclose.basic;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.BeanManager;
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

@RunWith(Arquillian.class)
public class AutoCloseTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(AutoCloseTest.class))
                .addPackage(AutoCloseTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Inject
    BeanManager beanManager;

    @Inject
    BeanContainer container;

    @Test
    public void testAutoCloseDetected() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans(AutoCloseableBean.class));
        assertTrue(bean.isAutoClose());
    }

    @Test
    public void testStereotypeAutoCloseDetected() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans(StereotypedBean.class));
        assertTrue(bean.isAutoClose());
    }

    @Test
    public void testManagedBeanAutoClose() {
        ActionSequence.reset();
        destroyViaHandle(AutoCloseableBean.class);
        ActionSequence.assertSequenceDataEquals("AutoCloseableBean.close");
    }

    @Test
    public void testAutoCloseOnNonAutoCloseableBean() {
        ActionSequence.reset();
        destroyViaHandle(NotAutoCloseableBean.class);
        assertNull(ActionSequence.getSequence());
    }

    @Test
    public void testNoAnnotationMeansNoClose() {
        ActionSequence.reset();
        destroyViaHandle(NoAnnotationCloseableBean.class);
        assertNull(ActionSequence.getSequence());
    }

    @Test
    public void testCloseExceptionSwallowed() {
        ActionSequence.reset();
        destroyViaHandle(ThrowingAutoCloseableBean.class);
        ActionSequence.assertSequenceDataEquals("ThrowingAutoCloseableBean.close");
    }

    @Test
    public void testProducerMethodAutoClose() {
        ActionSequence.reset();
        destroyViaBean(CloseableResource.class, ProducerMethodQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("producerMethod.close");
    }

    @Test
    public void testProducerFieldAutoClose() {
        ActionSequence.reset();
        destroyViaBean(CloseableResource.class, ProducerFieldQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("producerField.close");
    }

    @Test
    public void testDisposerCalledBeforeClose() {
        ActionSequence.reset();
        destroyViaBean(CloseableResource.class, WithDisposerQualifier.Literal.INSTANCE);
        ActionSequence.assertSequenceDataEquals("disposer", "withDisposer.close");
    }

    @Test
    public void testStereotypeAutoClose() {
        ActionSequence.reset();
        destroyViaHandle(StereotypedBean.class);
        ActionSequence.assertSequenceDataEquals("StereotypedBean.close");
    }

    @Test
    public void testPreDestroyCalledBeforeClose() {
        ActionSequence.reset();
        destroyViaHandle(PreDestroyAutoCloseableBean.class);
        ActionSequence.assertSequenceDataEquals("preDestroy", "close");
    }

    @Test
    public void testCloseCalledEvenWhenPreDestroyThrows() {
        ActionSequence.reset();
        destroyViaHandle(ThrowingPreDestroyAutoCloseableBean.class);
        ActionSequence.assertSequenceDataEquals("preDestroy.throwing", "ThrowingPreDestroyAutoCloseableBean.close");
    }

    @Test
    public void testCloseableInterfaceAutoClose() {
        ActionSequence.reset();
        destroyViaHandle(CloseableBean.class);
        ActionSequence.assertSequenceDataEquals("CloseableBean.close");
    }

    private <T> void destroyViaHandle(Class<T> type) {
        Instance<Object> instance = container.createInstance();
        Instance.Handle<T> handle = instance.select(type).getHandle();
        handle.get();
        handle.destroy();
    }

    @SuppressWarnings("unchecked")
    private <T> void destroyViaBean(Class<T> type, Annotation... qualifiers) {
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type, qualifiers));
        CreationalContext<T> cc = beanManager.createCreationalContext(bean);
        T instance = bean.create(cc);
        bean.destroy(instance, cc);
    }
}
