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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.mock.MockEELifecycle;
import org.jboss.weld.mock.TestContainer;
import org.testng.annotations.Test;

/**
 * @author kkhan
 *
 */
public class PreinstantiateBeanManagerTest
{

   @Test
   public void test()
   {
      //Start the container, but do not deploy weld yet
      List<Class<?>> classes = new ArrayList<Class<?>>();
      classes.add(WeldBean.class);
      TestContainer container = new TestContainer(new MockEELifecycle(), classes, null);
      container.getLifecycle().initialize();
      
      BeanManager bootstrapManager = container.getBeanManager();
      assert bootstrapManager != null;

      //Check the bean is null since it is not there yet
      Set<Bean<?>> beans = bootstrapManager.getBeans(WeldBean.class);
      assert beans == null || beans.size() == 0;
      
      
      //Start the application
      container.getLifecycle().beginApplication();
      container.ensureRequestActive();
      
      //Check the manager is the same as before
      BeanManager manager = container.getBeanManager();
      assert manager != null;
      assert bootstrapManager == manager;
      
      //Check we can get the bean
      Bean<? extends Object> bean = manager.resolve(manager.getBeans(WeldBean.class));
      WeldBean weldBean = (WeldBean) manager.getReference(bean, WeldBean.class, manager.createCreationalContext(bean));
      assert weldBean != null;
      
      container.stopContainer();
   }
   
}
