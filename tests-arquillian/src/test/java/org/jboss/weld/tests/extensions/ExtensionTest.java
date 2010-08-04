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
package org.jboss.weld.tests.extensions;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExtensionTest 
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
               .addPackage(ExtensionTest.class.getPackage())
               .addServiceProvider(Extension.class, 
                     SimpleExtension.class, 
                     ExtensionObserver.class,
                     WoodlandExtension.class);
   }

   /*
    * description = "WELD-234"
    */
   @Test 
   public void testExtensionInjectableAsBean(SimpleExtension extension)
   {
      assertTrue(extension.isObservedBeforeBeanDiscovery());
   }
   
   /*
    * description = "WELD-572"
    */
   @Test
   public void testGetNonExistentDisposalMethod(ExtensionObserver extensionObserver)
   {
      assertNull(extensionObserver.getProcessProducerMethodInstance().getAnnotatedDisposedParameter());
   }
      
   @Test
   public void testInjectionTargetWrapped(Capercaillie capercaillie)
   {
      assertTrue(Woodland.isPostConstructCalled());
      assertTrue(WoodlandExtension.isInjectCalled());
      assertTrue(WoodlandExtension.isPostConstructCalled());
      assertTrue(WoodlandExtension.isPreDestroyCalled());
      assertTrue(WoodlandExtension.isProduceCalled());
   }
}
