/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.el.resolver;

import static org.junit.Assert.assertEquals;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.inject.Inject;

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
 * Test the WeldELResolver and that it collaborates with the standard EL resolver chain.
 *
 * @author Pete Muir
 * @author Dan Allen
 */
@RunWith(Arquillian.class)
public class ELResolverTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ELResolverTest.class))
                .addPackage(ELResolverTest.class.getPackage())
                .addClass(EL.class)
                .addPackages(true, ExpressionFactory.class.getPackage());
    }

    @Inject
    private BeanManagerImpl beanManager;

    /**
     * Test that the WeldELResolver only works to resolve the base of an EL
     * expression, in this case a named bean. Once the base is resolved, the
     * remainder of the expression should be delegated to the standard chain of
     * property resolvers. If the WeldELResolver oversteps its bounds by
     * trying to resolve the property against the Weld namespace, the test
     * will fail.
     */
    @Test
    public void testResolveBeanPropertyOfNamedBean() {
        ELContext elContext = EL.createELContext(beanManager);
        ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;

        Object value = exprFactory.createValueExpression(elContext, "#{beer.style}", String.class).getValue(elContext);

        Assert.assertEquals("Belgium Strong Dark Ale", value);
    }

    @Test // WELD-874
    public void testResolveNormalScopedBean() {
        ELContext elContext = EL.createELContext(beanManager);
        ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;

        Lager value1 = (Lager) exprFactory.createValueExpression(elContext, "#{lager}", Lager.class).getValue(elContext);
        value1.drink();

        Lager value2 = (Lager) exprFactory.createValueExpression(elContext, "#{lager}", Lager.class).getValue(elContext);
        value2.drink();

        Lager value3 = (Lager) exprFactory.createValueExpression(elContext, "#{lager}", Lager.class).getValue(elContext);
        value3.drink();

        assertEquals(value1, value2);
        assertEquals(value2, value3);
    }

    /**
     * Test that the WeldELResolver only works to resolve the base of an EL
     * expression, in this case from a producer method. Once the base is
     * resolved, the remainder of the expression should be delegated to the
     * standard chain of property resolvers. If the WeldELResolver oversteps
     * its bounds by trying to resolve the property against the Weld
     * namespace, the test will fail.
     */
    @Test
    public void testResolveBeanPropertyOfProducerBean() {
        ELContext elContext = EL.createELContext(beanManager);
        ExpressionFactory exprFactory = EL.EXPRESSION_FACTORY;

        Object value = exprFactory.createValueExpression(elContext, "#{beerOnTap.style}", String.class).getValue(elContext);
        Assert.assertEquals("IPA", value);
    }

    @Test(expected = OrderException.class)
    // WELD-782
    public void testErrorMessageGood(BeanManagerImpl beanManager) {
        ELContext ctx = EL.createELContext(beanManager);
        EL.EXPRESSION_FACTORY.createValueExpression(ctx, "#{orderBean.orderId}", Object.class).getValue(ctx);
    }

    @Test
    public void testCompoundName(BeanManagerImpl beanManager) {
        ELContext ctx = EL.createELContext(beanManager);
        Object bean = EL.EXPRESSION_FACTORY.createValueExpression(ctx, "#{com.acme.settings}", Object.class).getValue(ctx);
        Assert.assertTrue(bean instanceof MyBean);
        Object foo = EL.EXPRESSION_FACTORY.createValueExpression(ctx, "#{com.acme.settings.foo}", Object.class).getValue(ctx);
        Assert.assertTrue(foo instanceof String);
        Assert.assertEquals("foo", foo);
    }
}
