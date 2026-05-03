package org.jboss.weld.tests.autoclose.interceptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
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
public class AutoCloseInterceptorTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(AutoCloseInterceptorTest.class))
                .addPackage(AutoCloseInterceptorTest.class.getPackage());
    }

    @Inject
    BeanManager beanManager;

    @Test
    public void testBusinessMethodStillIntercepted() {
        ActionSequence.reset();
        Bean<InterceptedAutoCloseableBean> bean = resolveBean();
        CreationalContext<InterceptedAutoCloseableBean> cc = beanManager.createCreationalContext(bean);
        InterceptedAutoCloseableBean instance = bean.create(cc);
        instance.ping();
        ActionSequence.assertSequenceDataContainsAll("interceptor.aroundInvoke");
        bean.destroy(instance, cc);
    }

    @Test
    public void testInterceptorNotInvokedDuringAutoClose() {
        ActionSequence.reset();
        Bean<InterceptedAutoCloseableBean> bean = resolveBean();
        CreationalContext<InterceptedAutoCloseableBean> cc = beanManager.createCreationalContext(bean);
        InterceptedAutoCloseableBean instance = bean.create(cc);
        bean.destroy(instance, cc);
        assertTrue("close() should have been called",
                ActionSequence.getSequenceData().contains("InterceptedAutoCloseableBean.close"));
        assertFalse("Interceptor should NOT fire during close() in destruction",
                ActionSequence.getSequenceData().contains("interceptor.aroundInvoke"));
    }

    @SuppressWarnings("unchecked")
    private Bean<InterceptedAutoCloseableBean> resolveBean() {
        return (Bean<InterceptedAutoCloseableBean>) beanManager.resolve(
                beanManager.getBeans(InterceptedAutoCloseableBean.class));
    }
}
