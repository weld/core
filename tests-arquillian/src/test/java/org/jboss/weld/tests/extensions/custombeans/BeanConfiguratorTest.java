/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.custombeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class BeanConfiguratorTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(BeanConfiguratorTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(BeanConfiguratorTest.class.getPackage())
                .addAsServiceProvider(Extension.class, BuilderExtension.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void testConfigurator(BeanManager beanManager) throws Exception {
        Set<Bean<?>> beans = beanManager.getBeans("bar");
        assertEquals(1, beans.size());
        Bean<Foo> fooBean = (Bean<Foo>) beans.iterator().next();
        assertEquals(Dependent.class, fooBean.getScope());
        Foo foo1 = (Foo) beanManager.getReference(fooBean, Foo.class, beanManager.createCreationalContext(fooBean));
        Foo foo2 = (Foo) beanManager.getReference(fooBean, Foo.class, beanManager.createCreationalContext(fooBean));
        assertFalse(foo1.getId().equals(foo2.getId()));

        beans = beanManager.getBeans(Foo.class, Juicy.Literal.INSTANCE);
        assertEquals(1, beans.size());
        fooBean = (Bean<Foo>) beans.iterator().next();
        Foo foo = (Foo) beanManager.getReference(fooBean, Foo.class, beanManager.createCreationalContext(fooBean));
        foo.ping();

        // bean is deliberately created via new creational context and BM.getReference
        beans = beanManager.getBeans(Integer.class, Random.Literal.INSTANCE);
        assertEquals(0, DependentBean.TIMES_DESTROY_INVOKED);
        assertEquals(1, beans.size());
        Bean<Integer> randomBean = (Bean<Integer>) beans.iterator().next();
        CreationalContext<Integer> ctx = beanManager.createCreationalContext(randomBean);
        Integer random = (Integer) beanManager.getReference(randomBean, Integer.class, ctx);
        assertNotNull(random);
        assertTrue(random >= 0 && random < 1000);
        assertEquals(0, DependentBean.TIMES_DESTROY_INVOKED);
        randomBean.destroy(random, ctx);
        assertTrue(BuilderExtension.DISPOSED.get());
        assertEquals(2, DependentBean.TIMES_DESTROY_INVOKED);

        // same as above but using Instance
        DependentBean.resetCounter();
        BuilderExtension.DISPOSED.set(false);
        Instance<Integer> integerInstance = beanManager.createInstance().select(Integer.class, Random.Literal.INSTANCE);
        assertEquals(0, DependentBean.TIMES_DESTROY_INVOKED);
        random = integerInstance.get();
        assertNotNull(random);
        assertTrue(random >= 0 && random < 1000);
        assertEquals(0, DependentBean.TIMES_DESTROY_INVOKED);
        integerInstance.destroy(random);
        assertTrue(BuilderExtension.DISPOSED.get());
        assertEquals(2, DependentBean.TIMES_DESTROY_INVOKED);

        // same as above but with plain injection
        DependentBean.resetCounter();
        BuilderExtension.DISPOSED.set(false);
        Instance<BeanInjectingSyntheticInteger> injectingBeanInstance = beanManager.createInstance().select(BeanInjectingSyntheticInteger.class);
        BeanInjectingSyntheticInteger bean = injectingBeanInstance.get();
        assertNotNull(bean);
        Integer beanValue = bean.getNumber();
        assertTrue(beanValue >= 0 && beanValue < 1000);
        injectingBeanInstance.destroy(bean);
        assertTrue(BuilderExtension.DISPOSED.get());
        assertEquals(2, DependentBean.TIMES_DESTROY_INVOKED);

        beans = beanManager.getBeans(Long.class, AnotherRandom.Literal.INSTANCE);
        assertEquals(1, beans.size());
        Bean<Long> anotherRandomBean = (Bean<Long>) beans.iterator().next();
        Long anotherRandom = (Long) beanManager.getReference(anotherRandomBean, Long.class,
                beanManager.createCreationalContext(anotherRandomBean));
        assertNotNull(anotherRandom);
        assertEquals(Long.valueOf(foo.getId() * 2), anotherRandom);

        beans = beanManager.getBeans(Bar.class);
        assertEquals(1, beans.size());
        Bean<Bar> barBean = (Bean<Bar>) beans.iterator().next();
        assertEquals(Dependent.class, barBean.getScope());

        beans = beanManager.getBeans(new TypeLiteral<List<String>>() {
        }.getType(), Juicy.Literal.INSTANCE);
        assertEquals(1, beans.size());
        Bean<List<String>> listBean = (Bean<List<String>>) beans.iterator().next();
        assertEquals(Dependent.class, listBean.getScope());
        List<String> list = (List<String>) beanManager.getReference(listBean, new TypeLiteral<List<String>>() {
        }.getType(), beanManager.createCreationalContext(listBean));
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("FOO", list.get(0));

        beans = beanManager.getBeans(VetoedBean.class, Random.Literal.INSTANCE);
        assertEquals(1, beans.size());
        fooBean = (Bean<Foo>) beans.iterator().next();
        assertEquals(Dependent.class, fooBean.getScope());
        Foo randomFoo = (Foo) beanManager.getReference(fooBean, Foo.class, beanManager.createCreationalContext(listBean));
        assertEquals(Long.valueOf(-1), randomFoo.getId());

        beans = beanManager.getBeans(Configuration.class);
        assertEquals(1, beans.size());
        Bean<Configuration> configBean = (Bean<Configuration>) beans.iterator().next();
        assertEquals(Dependent.class, configBean.getScope());
        Configuration configuration = (Configuration) beanManager.getReference(configBean, Configuration.class, beanManager.createCreationalContext(configBean));
        assertEquals(1, configuration.getId());

        beans = beanManager.getBeans(Integer.class, Bla.Literal.of("dependent"));
        assertEquals(1, beans.size());
        Bean<Configuration> blaBean = (Bean<Configuration>) beans.iterator().next();
        assertEquals(Dependent.class, blaBean.getScope());
        beans = beanManager.getBeans(Integer.class, Bla.Literal.of("model"));
        assertEquals(1, beans.size());
        blaBean = (Bean<Configuration>) beans.iterator().next();
        assertEquals(RequestScoped.class, blaBean.getScope());
        beans = beanManager.getBeans(Integer.class, Bla.Literal.of("more"));
        assertEquals(1, beans.size());
        blaBean = (Bean<Configuration>) beans.iterator().next();
        assertEquals(RequestScoped.class, blaBean.getScope());

        beans = beanManager.getBeans(String.class);
        assertEquals(1, beans.size());
        Bean<String> stringBean = (Bean<String>) beans.iterator().next();
        assertEquals(3, stringBean.getQualifiers().size());
    }

}
