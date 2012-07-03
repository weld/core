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
package org.jboss.weld.tests.session.newBean;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.producer.AbstractInjectionTarget;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class NewEnterpriseBeanTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(NewEnterpriseBeanTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static final New NEW_LITERAL = new NewLiteral(WrappedEnterpriseBean.class);

    @Inject
    private BeanManager beanManager;

    private SessionBean<WrappedEnterpriseBeanLocal> wrappedEnterpriseBean;
    private NewSessionBean<WrappedEnterpriseBeanLocal> newEnterpriseBean;

    public void initNewBean() {
        Set<Bean<?>> beans = beanManager.getBeans(WrappedEnterpriseBeanLocal.class);
        Assert.assertEquals(1, beanManager.getBeans(WrappedEnterpriseBeanLocal.class).size());
        Assert.assertTrue(beanManager.getBeans(WrappedEnterpriseBeanLocal.class).iterator().next() instanceof SessionBean<?>);
        wrappedEnterpriseBean = (SessionBean<WrappedEnterpriseBeanLocal>) beanManager.getBeans(WrappedEnterpriseBeanLocal.class).iterator().next();

        Assert.assertEquals(1, beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).size());
        Assert.assertTrue(beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next() instanceof NewSessionBean<?>);
        newEnterpriseBean = (NewSessionBean<WrappedEnterpriseBeanLocal>) beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next();
    }

    @Test
    public void testNewBeanHasImplementationClassOfInjectionPointType() {
        initNewBean();
        Assert.assertEquals(WrappedEnterpriseBean.class, newEnterpriseBean.getType());
    }

    @Test
    public void testNewBeanHasSameInitializerMethodsAsWrappedBean() {
        initNewBean();
        Assert.assertEquals(getInitializerMethods(wrappedEnterpriseBean), getInitializerMethods(newEnterpriseBean));
    }

    private List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods(Bean<?> bean) {
        if (bean instanceof AbstractClassBean<?>) {
            InjectionTarget<?> injectionTarget = Reflections.<AbstractClassBean<?>>cast(bean).getProducer();
            if (injectionTarget instanceof AbstractInjectionTarget<?>) {
                return Reflections.<AbstractInjectionTarget<?>>cast(injectionTarget).getInitializerMethods();
            }
        }
        throw new IllegalArgumentException(bean.toString());
    }

    @Test
    public void testNewBeanHasSameInjectedFieldsAsWrappedBean() {
        initNewBean();
        Set<InjectionPoint> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getInjectionPoints();
        Set<InjectionPoint> newBeanInjectionPoints = newEnterpriseBean.getInjectionPoints();
        Assert.assertEquals(wrappedBeanInjectionPoints, newBeanInjectionPoints);
    }

}
