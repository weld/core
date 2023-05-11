package org.jboss.weld.tests.contexts.dependent;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
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

import java.util.Set;

/**
 * Aims to indirectly verify that interceptors are dependent instances of beans they intercept.
 */
@RunWith(Arquillian.class)
public class DependentContextTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(DependentContextTest.class)).addPackage(DependentContextTest.class.getPackage());
    }

    @Inject
    BeanManager bm;

    @Test
    public void testDependentScopedInterceptorsAreDependentObjectsOfBean() {
        // since interceptors can't declare `@PreDestroy` callbacks for themselves, we inject
        // another dependent-scoped bean into the interceptor and add a `@PreDestroy` callback
        // there -- this dependency will be a dependent instance of the interceptor, and so
        // if destroying the intercepted bean will destroy the other bean, we have a proof
        // that the interceptor was also destroyed

        AccountTransaction.destroyed = false;
        TransactionalInterceptorDependency.destroyed = false;
        TransactionalInterceptor.intercepted = false;

        Set<Bean<?>> beans = bm.getBeans(AccountTransaction.class);
        Assert.assertTrue(beans.size() == 1);
        Bean<AccountTransaction> bean = (Bean<AccountTransaction>) beans.iterator().next();
        CreationalContext<AccountTransaction> ctx = bm.createCreationalContext(bean);

        AccountTransaction trans = (AccountTransaction) bm.getReference(bean, AccountTransaction.class, ctx);
        trans.execute();

        assert TransactionalInterceptor.intercepted;

        bean.destroy(trans, ctx);

        Assert.assertTrue(AccountTransaction.destroyed);
        Assert.assertTrue(TransactionalInterceptorDependency.destroyed);
    }
}
