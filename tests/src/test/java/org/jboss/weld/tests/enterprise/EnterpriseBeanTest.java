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

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.ear.EjbJarXml;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.test.Utils;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
@EjbJarXml("ejb-jar.xml")
public class EnterpriseBeanTest extends AbstractWeldTest
{
   
   @Test(description="WBRI-179")
   public void testSFSBWithOnlyRemoteInterfacesDeploys()
   {
      
   }
   
   @Test(description="WELD-326")
   public void testInvocationExceptionIsUnwrapped()
   {
      try
      {
         getReference(Fedora.class).causeRuntimeException();
      }
      catch (Throwable t)
      {
         if (t instanceof EJBException && t.getCause() instanceof BowlerHatException)
         {
            return;
         }
      }
      assert false : "Expected a BowlerHatException to be thrown";
   }   
   
   @Test(description="WBRI-275")
   public void testSLSBBusinessMethodThrowsRuntimeException()
   {
      try
      {
         getReference(Fedora.class).causeRuntimeException();
      }
      catch (Throwable t) 
      {
         if (Utils.isExceptionInHierarchy(t, BowlerHatException.class))
         {
            return;
         }
      }
      assert false : "Expected a BowlerHatException to be in the cause stack";
   }
   
   @Test(description="WELD-364", groups = "broken")
   public void testEJBRemoteInterfacesOkForObservers()
   {
      Feed feed = new Feed();
      getCurrentManager().fireEvent(feed);
      assert getReference(Scottish.class).getFeed().equals(feed);
   }
   
   @Test(description = "WELD-381")
   public void testGenericEJBWorks()
   {
      assert getReference(ResultClient.class).lookupPete().getUsername().equals("pete");
   }
   
   @Test(description = "WELD-80", groups = "broken")
   public void testPassivationOfEjbs()
   {
      HelloAction action = getReference(HelloAction.class);
      action.executeRequest();
      assert action.getHello().equals("hello");
      assert action.getGoodBye().equals("goodbye");
   }
   
   @Test(description = "Simple test for no-interface views", groups="broken")
   public void testNoInterfaceView()
   {
      Castle castle = getReference(Castle.class);
      castle.ping();
      assert castle.isPinged();
      assert getBean(Castle.class) instanceof SessionBean<?>;
   }
   
}
