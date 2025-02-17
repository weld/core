package org.jboss.weld.tests.el.injection;

import java.util.Set;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.el.ELAwareBeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.test.util.el.EL;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the injection of ELAwareBeanManager.
 *
 * @author Andrew Rouse
 */
@RunWith(Arquillian.class)
public class ELAwareBeanManagerInjectionTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ELAwareBeanManagerInjectionTest.class))
                .addPackage(ELAwareBeanManagerInjectionTest.class.getPackage())
                .addClass(EL.class)
                .addPackages(true, ExpressionFactory.class.getPackage());
    }

    @Inject
    private BeanManagerImpl beanManager;

    @Inject
    private ELAwareInjectionBean bean;

    @Test
    public void testInjection() {
        ELAwareBeanManager elBm = bean.getElAwareBeanManager();
        Assert.assertNotNull("elBm", elBm);

        ELResolver elResolver = elBm.getELResolver();
        Assert.assertNotNull("elResolver", elResolver);

        // Attempt to use the resolver
        ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;
        ELContext elContext = EL.createELContext(beanManager);

        ValueExpression exp = exprFactory.createValueExpression(elContext, "Result: ${testbean.value}", String.class);
        String value = exp.getValue(elContext);
        Assert.assertEquals("Result: hello", value);

        // Use it as a regular BeanManager (e.g. look up a bean)
        Set<Bean<?>> beans = beanManager.getBeans(ELAwareTestBean.class);
        Assert.assertEquals(1, beans.size());
        Bean<?> bean = beans.stream().findFirst().get();
        Assert.assertEquals("testbean", bean.getName());
    }
}
