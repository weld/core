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

import static org.jboss.weld.test.Utils.getActiveContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Sections
 * 
 * 6.5. Lifecycle of stateful session beans 
 * 6.6. Lifecycle of stateless session and singleton beans 
 * 6.11. Lifecycle of EJBs
 * 
 * Mostly overlapping with other tests...
 * 
 * @author Nicklas Karlsson
 * @author David Allen
 * 
 * Spec version: Public Release Draft 2
 * 
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class EnterpriseBeanLifecycleTest
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
         .addModule(
               ShrinkWrap.create(BeanArchive.class, "test.jar")
                  .decorate(AlarmedChickenHutch.class)
                  .addPackage(EnterpriseBeanLifecycleTest.class.getPackage())
                  .addClass(Utils.class)
         );
   }

   @Inject 
   private BeanManagerImpl beanManager;
   
   /**
    * When the create() method of a Bean object that represents a stateful
    * session bean that is called, the container creates and returns a session
    * bean proxy, as defined in Section 3.3.9, "Session bean proxies".
    */
   @Test
   public void testCreateSFSB(GrossStadt frankfurt)
   {
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
   public void testDestroyRemovesSFSB(GrossStadt frankfurt) throws Exception
   {
      RequestContext requestContext = getActiveContext(beanManager, RequestContext.class);
      Bean<KleinStadt> stadtBean = Utils.getBean(beanManager, KleinStadt.class);
      assertNotNull("Expected a bean for stateful session bean Kassel", stadtBean);
      CreationalContext<KleinStadt> creationalContext = new MockCreationalContext<KleinStadt>();
      KleinStadt kassel = requestContext.get(stadtBean, creationalContext);
      stadtBean.destroy(kassel, creationalContext);
      
      assertTrue("Expected SFSB bean to be destroyed", frankfurt.isKleinStadtDestroyed());
      
      // TODO Make this into a remote test
      /*requestContext.invalidate();
      requestContext.deactivate();
      requestContext.activate();
      kassel = requestContext.get(stadtBean);
      assertNull("SFSB bean should not exist after being destroyed", kassel);*/
   }
   
   @Test
   public void testDestroyDoesntTryToRemoveSLSB()
   {
      Bean<BeanLocal> bean = Utils.getBean(beanManager, BeanLocal.class);
      Assert.assertNotNull("Expected a bean for stateless session bean BeanLocal", bean);
      CreationalContext<BeanLocal> creationalContext = beanManager.createCreationalContext(bean);
      BeanLocal instance = bean.create(creationalContext);
      bean.destroy(instance, creationalContext);
   }
   
   @Inject @MassProduced Instance<ChickenHutch> chickenHutchInstance;
   
   @Test
   // WELD-556
   public void testDecoratedSFSBsAreRemoved()
   {
      StandardChickenHutch.reset();
      AlarmedChickenHutch.reset();
      chickenHutchInstance.get();
      assert StandardChickenHutch.isPing();
      assert AlarmedChickenHutch.isPing();
      assert StandardChickenHutch.isPredestroy(); 
   }
 
}
