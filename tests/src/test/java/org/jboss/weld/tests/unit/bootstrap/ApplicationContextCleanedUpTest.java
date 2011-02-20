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
package org.jboss.weld.tests.unit.bootstrap;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Assert;

import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.TestContainer;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.unbound.ApplicationContextImpl;
import org.testng.annotations.Test;

public class ApplicationContextCleanedUpTest
{

   @Test
   public void testApplicationContextCleanedUp()
   {
      TestContainer container = new TestContainer(Foo.class, Bar.class);
      container.startContainer();

      BeanManager manager = getBeanManager(container);
      Bean<ApplicationContextImpl> bean = (Bean<ApplicationContextImpl>) manager.resolve(manager.getBeans(ApplicationContext.class));
      ApplicationContextImpl appContext = (ApplicationContextImpl) manager.getReference(bean, ApplicationContext.class, manager.createCreationalContext(bean));

      container.stopContainer();

      Assert.assertTrue(appContext.isCleanedUp());

   }

   /**
    * Get the bean manager, assuming a flat deployment structure
    */
   public static BeanManager getBeanManager(TestContainer container)
   {
      return container.getBeanManager(container.getDeployment().getBeanDeploymentArchives().iterator().next());
   }

}
