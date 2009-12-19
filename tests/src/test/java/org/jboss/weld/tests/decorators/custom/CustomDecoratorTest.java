/*
 * JBoss, Home of Professional Open Source
 * Copyright <Year>, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.tests.decorators.custom;

import java.util.Arrays;

import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.MockBeanDeploymentArchive;
import org.jboss.weld.mock.MockDeployment;
import org.jboss.weld.util.serviceProvider.ServiceLoaderFactory;
import org.jboss.weld.util.serviceProvider.PackageServiceLoaderFactory;
import org.jboss.weld.BeanManagerImpl;

import org.testng.annotations.Test;

/**
 * @author Marius Bogoevici
 */
public class CustomDecoratorTest
{

   @Test
   public void testCustomDecoratorAppliedByItself()
   {
      MockBeanDeploymentArchive beanDeploymentArchive = new MockBeanDeploymentArchive("1", WindowImpl.class, CustomWindowFrame.class, InnerWindowFrame.class, OuterWindowFrame.class );
      beanDeploymentArchive.setBeansXmlFiles(Arrays.asList(CustomDecoratorTest.class.getResource("beans-custom-only.xml")));
      TestContainer testContainer = new TestContainer(new MockServletLifecycle(new MockDeployment(beanDeploymentArchive), beanDeploymentArchive));
      testContainer.getLifecycle().initialize();
      testContainer.getDeployment().getServices().add(ServiceLoaderFactory.class, new PackageServiceLoaderFactory(CustomDecoratorTest.class.getPackage(), Extension.class));
      testContainer.getLifecycle().beginApplication();

      BeanManagerImpl beanManager = testContainer.getBeanManager();
      Bean<Object> windowBean = (Bean<Object>) beanManager.getBeans(WindowImpl.class).iterator().next();
      CreationalContext<Object> creationalContext = beanManager.createCreationalContext(windowBean);
      WindowImpl window  = (WindowImpl) windowBean.create(creationalContext);
      window.draw();

      assert window.isDrawn();
      assert CustomWindowFrame.drawn;
      testContainer.stopContainer();
   }

   @Test
   public void testCustomDecoratorAppliedWithWeldDecorators()
   {
      MockBeanDeploymentArchive beanDeploymentArchive = new MockBeanDeploymentArchive("1", WindowImpl.class, CustomWindowFrame.class, InnerWindowFrame.class, OuterWindowFrame.class );
      beanDeploymentArchive.setBeansXmlFiles(Arrays.asList(CustomDecoratorTest.class.getResource("beans.xml")));
      TestContainer testContainer = new TestContainer(new MockServletLifecycle(new MockDeployment(beanDeploymentArchive), beanDeploymentArchive));
      testContainer.getLifecycle().initialize();
      testContainer.getDeployment().getServices().add(ServiceLoaderFactory.class, new PackageServiceLoaderFactory(CustomDecoratorTest.class.getPackage(), Extension.class));      
      testContainer.getLifecycle().beginApplication();

      BeanManagerImpl beanManager = testContainer.getBeanManager();
      Bean<Object> windowBean = (Bean<Object>) beanManager.getBeans(WindowImpl.class).iterator().next();
      CreationalContext<Object> creationalContext = beanManager.createCreationalContext(windowBean);
      WindowImpl window  = (WindowImpl) windowBean.create(creationalContext);
      window.draw();

      assert window.isDrawn();
      assert OuterWindowFrame.drawn;
      assert InnerWindowFrame.drawn;
      assert CustomWindowFrame.drawn;

      testContainer.stopContainer();
   }

}
