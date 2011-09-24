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
package org.jboss.weld.tests.extensions.injectionTarget;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.jboss.weld.tests.category.Broken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

//@Category(Integration.class)
@Category(Broken.class)
@RunWith(Arquillian.class)
public class InjectionTargetTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(
                        ShrinkWrap.create(BeanArchive.class)
                                .intercept(SecurityInterceptor.class)
                                .decorate(AircraftDecorator.class)
                                .addPackage(InjectionTargetTest.class.getPackage())
                                .addClass(Utils.class)
                                .addAsServiceProvider(Extension.class, InjectionTargetExtension.class)
                );
    }

    @Inject
    private BeanManagerImpl beanManager;

    /*
    * description = "WELD-557"
    */
    @Test
    public void testActualInstanceAndNotProxyPassedToInject() {
        InjectionTargetWrapper.clear();
        Spitfire aircraft = Utils.getReference(beanManager, Spitfire.class);
        aircraft.isFlying();
        Assert.assertTrue(aircraft.isTheSameInstance(InjectionTargetWrapper.injectInstance));
    }

    /*
    * description = "WELD-557"
    */
    @Test
    public void testActualInstanceAndNotProxyPassedToPostConstruct() {
        InjectionTargetWrapper.clear();
        Spitfire aircraft = Utils.getReference(beanManager, Spitfire.class);
        aircraft.isFlying();
        Assert.assertTrue(aircraft.isTheSameInstance(InjectionTargetWrapper.postConstructInstance));
    }

    /*
    * description = "WELD-557"
    */
    //
    @Test
    public void testActualInstanceAndNotProxyPassedToPreDestroy() {
        // prepare instance
        InjectionTargetWrapper.clear();
        Bean<Spitfire> bean = Utils.getBean(beanManager, Spitfire.class);
        CreationalContext<Spitfire> ctx = beanManager.createCreationalContext(bean);
        Spitfire aircraft = (Spitfire) beanManager.getReference(bean, Spitfire.class, ctx);
        // invoke business method
        aircraft.isFlying();
        // destroy instance
        bean.destroy(aircraft, ctx);

        Assert.assertTrue(aircraft.isTheSameInstance(InjectionTargetWrapper.preDestroyInstance));
    }
}
