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

import javax.ejb.EJBException;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class EnterpriseBeanTest 
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
         .addModule(
               ShrinkWrap.create(JavaArchive.class)
                  .addPackage(EnterpriseBeanTest.class.getPackage())
                  .addClass(Utils.class)
                  .addManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                  .addManifestResource(EjbDescriptorLookupTest.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml")
         );
   }

   @Inject
   private BeanManagerImpl beanManager;
   
   @Inject
   private Event<Fuel> refuel;
   /*
    * description="WBRI-179"
    */
   @Test
   public void testSFSBWithOnlyRemoteInterfacesDeploys()
   {
      // TODO: Need implementation ?
   }
   
   /*
    * description="WELD-326"
    */
   @Test
   public void testInvocationExceptionIsUnwrapped(Fedora fedora)
   {
      try
      {
         fedora.causeRuntimeException();
      }
      catch (Throwable t)
      {
         if (t instanceof EJBException && t.getCause() instanceof BowlerHatException)
         {
            return;
         }
      }
      Assert.fail("Expected a BowlerHatException to be thrown");
   }   
   
   /*
    * description="WBRI-275"
    */
   @Test
   public void testSLSBBusinessMethodThrowsRuntimeException(Fedora fedora)
   {
      try
      {
         fedora.causeRuntimeException();
      }
      catch (Throwable t) 
      {
         if (Utils.isExceptionInHierarchy(t, BowlerHatException.class))
         {
            return;
         }
      }
      Assert.fail("Expected a BowlerHatException to be in the cause stack");
   }
   
   /*
    * description = "WELD-381"
    */
   @Test
   public void testGenericEJBWorks(ResultClient client)
   {
      Assert.assertEquals("pete", client.lookupPete().getUsername());
   }
   
   /*
    * description = "WELD-80"
    */
   @Test
   @Category(Broken.class)
   public void testPassivationOfEjbs(HelloAction action)
   {
      action.executeRequest();
      Assert.assertEquals("hello", action.getHello());
      Assert.assertEquals("goodbye", action.getGoodBye());
   }
   
   /*
    * description = "Simple test for no-interface views"
    */
   @Test
   @Category(Broken.class)
   public void testNoInterfaceView(Castle castle)
   {
      castle.ping();
      Assert.assertTrue(castle.isPinged());
      Assert.assertTrue(Utils.getBean(beanManager, Castle.class) instanceof SessionBean<?>);
   }
   
   @Test
   // WELD-364
   public void testRemoteEjbMethodCanObserve(FarmMachine farmMachine)
   {
      Fuel fuel = new Fuel("Unleaded");
      refuel.fire(fuel);
      assert farmMachine.getObservedFuel().getType().equals("Unleaded");
   }
   
   @Test
   // WELD-364
   public void testRemoteEjbMethodCanProduceAndDispose()
   {
      Bean<Fumes> fumesBean = Utils.getBean(beanManager, Fumes.class);
      CreationalContext<Fumes> ctx = beanManager.createCreationalContext(fumesBean);
      Fumes fumes = fumesBean.create(ctx);
      assert fumes.getVolume() == 10;
      fumesBean.destroy(fumes, ctx);
      assert fumes.getVolume() == 5;
      
   }
   
}
