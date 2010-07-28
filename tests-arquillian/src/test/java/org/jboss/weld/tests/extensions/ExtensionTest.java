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

import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class ExtensionTest 
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
         .addModule(
               ShrinkWrap.create(BeanArchive.class)
                  .addPackage(ExtensionTest.class.getPackage())
                  .addServiceProvider(Extension.class, 
                        SimpleExtension.class, 
                        ExtensionObserver.class,
                        WoodlandExtension.class)
         );
   }

   /*
    * description = "WELD-234"
    */
   @Test
   public void testExtensionInjectableAsBean(SimpleExtension extension)
   {
      Assert.assertTrue(extension.isObservedBeforeBeanDiscovery());
   }
   
   /*
    * description = "WELD-243"
    */
   @Test
   public void testContainerEventsOnlySentToExtensionBeans(ExtensionObserver extensionObserver, OtherObserver otherObserver)
   {
      Assert.assertTrue(extensionObserver.isBeforeBeanDiscovery());
      Assert.assertTrue(extensionObserver.isAllBeforeBeanDiscovery());
      Assert.assertFalse(otherObserver.isBeforeBeanDiscovery());
      Assert.assertFalse(otherObserver.isAllBeforeBeanDiscovery());
      
      Assert.assertTrue(extensionObserver.isAfterBeanDiscovery());
      Assert.assertTrue(extensionObserver.isAllAfterBeanDiscovery());
      Assert.assertFalse(otherObserver.isAfterBeanDiscovery());
      Assert.assertFalse(otherObserver.isAllAfterBeanDiscovery());
      
      Assert.assertTrue(extensionObserver.isProcessAnnotatedType());
      Assert.assertTrue(extensionObserver.isAllProcessAnnnotatedType());
      Assert.assertFalse(otherObserver.isProcessAnnotatedType());
      Assert.assertFalse(otherObserver.isAllProcessAnnotatedType());
      
      Assert.assertTrue(extensionObserver.isProcessBean());
      Assert.assertTrue(extensionObserver.isAllProcessBean());
      Assert.assertFalse(otherObserver.isProcessBean());
      Assert.assertFalse(otherObserver.isAllProcessBean());
      
      Assert.assertTrue(extensionObserver.isProcessInjectionTarget());
      Assert.assertTrue(extensionObserver.isAllProcessInjectionTarget());
      Assert.assertFalse(otherObserver.isProcessInjectionTarget());
      Assert.assertFalse(otherObserver.isAllProcessInjectionTarget());
      
      Assert.assertTrue(extensionObserver.isProcessManagedBean());
      Assert.assertTrue(extensionObserver.isAllProcessManagedBean());
      Assert.assertFalse(otherObserver.isProcessManagedBean());
      Assert.assertFalse(otherObserver.isAllProcessManagedBean());
      
      Assert.assertTrue(extensionObserver.isProcessObserverMethod());
      Assert.assertTrue(extensionObserver.isAllProcessObserverMethod());
      Assert.assertFalse(otherObserver.isProcessObserverMethod());
      Assert.assertFalse(otherObserver.isAllProcessObserverMethod());
      
      Assert.assertTrue(extensionObserver.isProcessProducer());
      Assert.assertTrue(extensionObserver.isAllProcessProducer());
      Assert.assertFalse(otherObserver.isProcessProducer());
      Assert.assertFalse(otherObserver.isAllProcessProducer());
      
      Assert.assertTrue(extensionObserver.isProcessProducerField());
      Assert.assertTrue(extensionObserver.isAllProcessProducerField());
      Assert.assertFalse(otherObserver.isProcessProducerField());
      Assert.assertFalse(otherObserver.isAllProcessProducerField());
      
      Assert.assertTrue(extensionObserver.isProcessProducerMethod());
      Assert.assertTrue(extensionObserver.isAllProcessProducerField());
      Assert.assertFalse(otherObserver.isProcessProducerMethod());
      Assert.assertFalse(otherObserver.isAllProcessProducerMethod());
      
      Assert.assertTrue(extensionObserver.isProcessSessionBean());
      Assert.assertTrue(extensionObserver.isAllProcessSessionBean());
      Assert.assertFalse(otherObserver.isProcessSessionBean());
      Assert.assertFalse(otherObserver.isAllProcessSessionBean());
      
      Assert.assertTrue(extensionObserver.isAfterDeploymentValidation());
      Assert.assertTrue(extensionObserver.isAllAfterDeploymentValidation());
      Assert.assertFalse(otherObserver.isAfterDeploymentValidation());
      Assert.assertFalse(otherObserver.isAllAfterDeploymentValidation());
   }
   
   @Test
   public void testInjectionTargetWrapped(Capercaillie capercaillie)
   {
      Assert.assertTrue(Woodland.isPostConstructCalled());
      Assert.assertTrue(WoodlandExtension.isInjectCalled());
      Assert.assertTrue(WoodlandExtension.isPostConstructCalled());
      Assert.assertTrue(WoodlandExtension.isPreDestroyCalled());
      Assert.assertTrue(WoodlandExtension.isProduceCalled());
   }
}
