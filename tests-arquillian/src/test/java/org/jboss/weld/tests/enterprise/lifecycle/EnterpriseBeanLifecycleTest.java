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
package org.jboss.weld.tests.enterprise.lifecycle;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Sections
 * <p/>
 * 6.5. Lifecycle of stateful session beans
 * 6.6. Lifecycle of stateless session and singleton beans
 * 6.11. Lifecycle of EJBs
 * <p/>
 * Mostly overlapping with other tests...
 *
 * @author Nicklas Karlsson
 * @author David Allen
 *         <p/>
 *         Spec version: Public Release Draft 2
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class EnterpriseBeanLifecycleTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EnterpriseBeanLifecycleTest.class))
                .beanDiscoveryMode(BeanDiscoveryMode.ALL)
                .decorate(AlarmedChickenHutch.class)
                .addPackage(EnterpriseBeanLifecycleTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Inject
    private BeanManagerImpl beanManager;

    /**
     * When the create() method of a Bean object that represents a stateful
     * session bean that is called, the container creates and returns a session
     * bean proxy, as defined in Section 3.3.9, "Session bean proxies".
     */
    @Test
    public void testCreateSFSB(GrossStadt frankfurt) {
        Bean<KleinStadt> stadtBean = Utils.getBean(beanManager, KleinStadt.class);
        Assert.assertNotNull("Expected a bean for stateful session bean Kassel", stadtBean);
        CreationalContext<KleinStadt> creationalContext = new MockCreationalContext<KleinStadt>();
        KleinStadt stadtInstance = stadtBean.create(creationalContext);
        Assert.assertNotNull("Expected instance to be created by container", stadtInstance);
        Assert.assertTrue("PostConstruct should be invoked when bean instance is created", frankfurt.isKleinStadtCreated());
        frankfurt.resetCreatedFlags();

        // Create a second one to make sure create always does create a new session bean
        KleinStadt anotherStadtInstance = stadtBean.create(creationalContext);
        Assert.assertNotNull("Expected second instance of session bean", anotherStadtInstance);
        Assert.assertTrue(frankfurt.isKleinStadtCreated());
        Assert.assertNotSame("create() should not return same bean as before", anotherStadtInstance, stadtInstance);

        // Verify that the instance returned is a proxy by checking for all local interfaces
        Assert.assertTrue(stadtInstance instanceof KleinStadt);
        Assert.assertTrue(stadtInstance instanceof SchoeneStadt);
    }

    @Test
    public void testDestroyDoesntTryToRemoveSLSB() {
        Bean<BeanLocal> bean = Utils.getBean(beanManager, BeanLocal.class);
        Assert.assertNotNull("Expected a bean for stateless session bean BeanLocal", bean);
        CreationalContext<BeanLocal> creationalContext = beanManager.createCreationalContext(bean);
        BeanLocal instance = bean.create(creationalContext);
        bean.destroy(instance, creationalContext);
    }

    @Test
    // WELD-556
    public void testDecoratedSFSBsAreRemoved(BeanManager manager) {
        StandardChickenHutch.reset();
        AlarmedChickenHutch.reset();

        Bean<ChickenHutch> bean = Reflections.cast(manager.resolve(manager.getBeans(ChickenHutch.class)));
        CreationalContext<ChickenHutch> cc = manager.createCreationalContext(bean);
        ChickenHutch instance = bean.create(cc);

        instance.ping();
        assert StandardChickenHutch.isPing();
        assert AlarmedChickenHutch.isPing();

        bean.destroy(instance, cc);
        assert StandardChickenHutch.isPredestroy();
    }

}
