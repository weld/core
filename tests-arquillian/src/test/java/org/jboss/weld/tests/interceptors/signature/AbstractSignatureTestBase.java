package org.jboss.weld.tests.interceptors.signature;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class AbstractSignatureTestBase {
    @Inject
    private BeanManager beanManager;

    protected <T> T getBean(Class<T> beanType) {
        Set<Bean<?>> beans = beanManager.getBeans(beanType);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<Object> creationalContext = beanManager.createCreationalContext(null);
        try {
            return (T) beanManager.getReference(bean, beanType, creationalContext);
        } finally {
            creationalContext.release();
        }
    }

    protected void assertNotInvoked(boolean invoked) {
        assertFalse("interceptor method should not have been invoked, but it was", invoked);
    }

    protected void assertInvoked(boolean invoked) {
        assertTrue("interceptor method should have been invoked, but it wasn't", invoked);
    }
}
