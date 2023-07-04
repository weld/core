package org.jboss.weld.tests.beanManager.getReference.interceptor;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * NOTE: The functionality tested here (using BM#getReference for interceptor instances) isn't rooted in CDI spec but
 * seems to be something intergrators have relied on in the past. One such example is GF resolving interceptors as part
 * of their EJB integration. Another case used to be MP REST using this to simulate their own interceptor chain.
 */
@RunWith(Arquillian.class)
public class ManualInterceptorInstanceRetrievalTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(ManualInterceptorInstanceRetrievalTest.class)).addPackage(ManualInterceptorInstanceRetrievalTest.class.getPackage());
    }

    @Inject
    BeanManager bm;

    @Test
    public void testGetReferenceForInterceptorInstance() {
        List<Interceptor<?>> interceptors = bm.resolveInterceptors(InterceptionType.AROUND_INVOKE, MyBinding.Literal.INSTANCE);
        Assert.assertTrue(interceptors.size() == 1);
        Interceptor<?> interceptor = interceptors.get(0);
        CreationalContext<?> creationalContext = bm.createCreationalContext(interceptor);
        Object reference = bm.getReference(interceptor, MyInterceptor.class, creationalContext);
        Assert.assertNotNull(reference);
    }
}
