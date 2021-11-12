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
package org.jboss.weld.tests.enterprise;

import static org.junit.Assert.assertNotNull;

import jakarta.ejb.EJBException;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.util.BeanPassivator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class EnterpriseBeanTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EnterpriseBeanTest.class))
                .beanDiscoveryMode(BeanDiscoveryMode.ALL)
                .addPackage(EnterpriseBeanTest.class.getPackage())
                .addClasses(Utils.class, BeanPassivator.class);
    }

    @Inject
    private BeanManagerImpl beanManager;

    /*
    * description="WELD-326"
    */
    @Test
    public void testInvocationExceptionIsUnwrapped(Fedora fedora) {
        try {
            fedora.causeRuntimeException();
        } catch (Throwable t) {
            if (t instanceof EJBException && t.getCause() instanceof BowlerHatException) {
                return;
            }
        }
        Assert.fail("Expected a BowlerHatException to be thrown");
    }

    /*
    * description="WBRI-275"
    */
    @Test
    public void testSLSBBusinessMethodThrowsRuntimeException(Fedora fedora) {
        try {
            fedora.causeRuntimeException();
        } catch (Throwable t) {
            if (Utils.isExceptionInHierarchy(t, BowlerHatException.class)) {
                return;
            }
        }
        Assert.fail("Expected a BowlerHatException to be in the cause stack");
    }

    /*
    * description = "WELD-364"
    */
    @Test
    public void testEJBRemoteInterfacesOkForObservers(Scottish scottish) {
        Feed feed = new Feed();
        beanManager.getEvent().select(Feed.class).fire(feed);
        Assert.assertEquals(feed, scottish.getFeed());
    }

    /*
    * description = "WELD-381"
    */
    @Test
    public void testGenericEJBWorks(ResultClient client) {
        Assert.assertEquals("pete", client.lookupPete().getUsername());
    }

    /*
    * description = "Test for passivation of SFSB"
    */
    @Test
    public void testPassivationOfEjbs(HelloAction action) {
        action.executeRequest();
        Assert.assertEquals("hello", action.getHello());
        Assert.assertEquals("goodbye", action.getGoodBye());
    }

    /*
    * description = "Simple test for no-interface views"
    */
    @Test
    public void testNoInterfaceView(Castle castle) {
        castle.ping();
        Assert.assertTrue(castle.isPinged());
        Assert.assertTrue(Utils.getBean(beanManager, Castle.class) instanceof SessionBean<?>);
    }

    @Test
    // WELD-492
    // ARQ-258
    public void testImplementsEnterpriesBean(Grault grault) {
        assertNotNull(grault);
    }

}
