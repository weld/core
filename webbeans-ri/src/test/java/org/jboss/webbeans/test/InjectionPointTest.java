/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.InjectionPoint;
import javax.webbeans.Standard;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.binding.CurrentBinding;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.beans.BeanWithInjectionPointMetadata;
import org.jboss.webbeans.test.beans.ConstructorInjectionPointBean;
import org.jboss.webbeans.test.beans.FieldInjectionPointBean;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;
import org.testng.annotations.Test;

/**
 * Injection point metadata tests
 * 
 * @author David Allen
 * 
 */
@SpecVersion("20081222")
public class InjectionPointTest extends AbstractTest
{
   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetInstance()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert beanWithInjectionPoint.getInjectedMetadata().getInstance().equals(beanWithInjectedBean);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetBean()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;

         Set<Bean<FieldInjectionPointBean>> theBean = manager.resolveByType(FieldInjectionPointBean.class);
         assert beanWithInjectionPoint.getInjectedMetadata().getBean().equals(theBean);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetType()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert beanWithInjectionPoint.getInjectedMetadata().getType().equals(FieldInjectionPointBean.class);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetBindingTypes()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         Set<Annotation> bindingTypes = beanWithInjectionPoint.getInjectedMetadata().getBindingTypes();
         assert bindingTypes.size() == 1;
         assert bindingTypes.iterator().next().equals(Current.class);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetMemberField()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert Field.class.isAssignableFrom(beanWithInjectionPoint.getInjectedMetadata().getMember().getClass());
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "stub", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetMemberMethod()
   {
      assert false;
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetMemberConstructor()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(ConstructorInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         ConstructorInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(ConstructorInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert Constructor.class.isAssignableFrom(beanWithInjectionPoint.getInjectedMetadata().getMember().getClass());
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetAnnotation()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert beanWithInjectionPoint.getInjectedMetadata().getAnnotation(AnimalStereotype.class) != null;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testGetAnnotations()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         Set<Annotation> annotations = new HashSet<Annotation>(Arrays.asList(beanWithInjectionPoint.getInjectedMetadata().getAnnotations()));
         assert annotations.size() > 0;
         assert annotations.contains(Current.class);
         assert annotations.contains(AnimalStereotype.class);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testStandardDeployment()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert beanWithInjectionPoint.getInjectedMetadata().getBean().getDeploymentType().equals(Standard.class);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testDependentScope()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert beanWithInjectionPoint.getInjectedMetadata().getBean().getScopeType().equals(Dependent.class);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testApiTypeInjectionPoint()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert beanWithInjectionPoint.getInjectedMetadata().getBean().getTypes().contains(InjectionPoint.class);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Test(groups = { "broken", "injectionPoint" })
   @SpecAssertion(section = "5.11")
   public void testCurrentBinding()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(FieldInjectionPointBean.class, BeanWithInjectionPointMetadata.class));
      webBeansBootstrap.boot();

      // Get an instance of the bean which has another bean injected into it
      try
      {
         DependentContext.INSTANCE.setActive(true);
         FieldInjectionPointBean beanWithInjectedBean = manager.getInstanceByType(FieldInjectionPointBean.class, new CurrentBinding());
         BeanWithInjectionPointMetadata beanWithInjectionPoint = beanWithInjectedBean.getInjectedBean();
         assert beanWithInjectionPoint.getInjectedMetadata() != null;
         assert beanWithInjectionPoint.getInjectedMetadata().getBean().getBindingTypes().contains(Current.class);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }
}
