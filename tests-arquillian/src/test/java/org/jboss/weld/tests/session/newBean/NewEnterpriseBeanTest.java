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
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class) // all EJB tests need to use this category because the arquillian-weld-ee container does not implement EjbDescriptor.isPassivationCapable()
public class NewEnterpriseBeanTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(NewEnterpriseBeanTest.class))
                .addPackage(NewEnterpriseBeanTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static final New NEW_LITERAL = New.Literal.of(WrappedEnterpriseBean.class);

    @Inject
    private BeanManager beanManager;

    private SessionBean<WrappedEnterpriseBeanLocal> wrappedEnterpriseBean;
    private SessionBean<WrappedEnterpriseBeanLocal> newEnterpriseBean;

    public void initNewBean() {
        Assert.assertEquals(1, beanManager.getBeans(WrappedEnterpriseBeanLocal.class).size());
        Assert.assertTrue(beanManager.getBeans(WrappedEnterpriseBeanLocal.class).iterator().next() instanceof SessionBean<?>);
        wrappedEnterpriseBean = (SessionBean<WrappedEnterpriseBeanLocal>) beanManager.getBeans(WrappedEnterpriseBeanLocal.class).iterator().next();

        Assert.assertEquals(1, beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).size());
        Assert.assertTrue(beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next() instanceof NewBean);
        newEnterpriseBean = (SessionBean<WrappedEnterpriseBeanLocal>) beanManager.getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next();
    }

    @Test
    public void testNewBeanHasImplementationClassOfInjectionPointType() {
        initNewBean();
        Assert.assertEquals(WrappedEnterpriseBean.class, newEnterpriseBean.getBeanClass());
    }

    @Test
    public void testNewBeanHasSameInitializerMethodsAsWrappedBean() {
        initNewBean();
        Assert.assertEquals(getInitializerMethods(wrappedEnterpriseBean), getInitializerMethods(newEnterpriseBean));
    }

    private List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods(Bean<?> bean) {
        if (bean instanceof AbstractClassBean<?>) {
            InjectionTarget<?> injectionTarget = Reflections.<AbstractClassBean<?>>cast(bean).getProducer();
            if (injectionTarget instanceof BasicInjectionTarget<?>) {
                return Reflections.<BasicInjectionTarget<?>>cast(injectionTarget).getInjector().getInitializerMethods();
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
