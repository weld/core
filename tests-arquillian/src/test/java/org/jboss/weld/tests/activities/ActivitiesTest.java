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
package org.jboss.weld.tests.activities;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.ForwardingBean;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.jboss.weld.util.collections.Arrays2;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Spec version: 20090519
 *
 */
@RunWith(Arquillian.class)
public class ActivitiesTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(ActivitiesTest.class.getPackage())
         .addClass(Utils.class);
   }

   private static final Set<Annotation> DEFAULT_QUALIFIERS = Collections.<Annotation> singleton(DefaultLiteral.INSTANCE);

   private Bean<?> createDummyBean(BeanManager beanManager, final Type injectionPointType)
   {
      final Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();
      final Set<Type> types = new HashSet<Type>();
      final Set<Annotation> bindings = new HashSet<Annotation>();
      bindings.add(new AnnotationLiteral<Tame>()
      {
      });
      types.add(Object.class);
      final Bean<?> bean = new Bean<Object>()
      {

         public Set<Annotation> getQualifiers()
         {
            return bindings;
         }

         public Set<InjectionPoint> getInjectionPoints()
         {
            return injectionPoints;
         }

         public String getName()
         {
            return null;
         }

         public Class<? extends Annotation> getScope()
         {
            return Dependent.class;
         }

         public Set<Type> getTypes()
         {
            return types;
         }

         public boolean isNullable()
         {
            return false;
         }

         public Object create(CreationalContext<Object> creationalContext)
         {
            return null;
         }

         public void destroy(Object instance, CreationalContext<Object> creationalContext)
         {

         }

         public Class<?> getBeanClass()
         {
            return Object.class;
         }

         public boolean isAlternative()
         {
            return false;
         }

         public Set<Class<? extends Annotation>> getStereotypes()
         {
            return Collections.emptySet();
         }

      };
      InjectionPoint injectionPoint = new InjectionPoint()
      {

         public Bean<?> getBean()
         {
            return bean;
         }

         public Set<Annotation> getQualifiers()
         {
            return DEFAULT_QUALIFIERS;
         }

         public Member getMember()
         {
            return null;
         }

         public Type getType()
         {
            return injectionPointType;
         }

         public Annotated getAnnotated()
         {
            return null;
         }

         public boolean isDelegate()
         {
            return false;
         }

         public boolean isTransient()
         {
            return false;
         }

      };
      injectionPoints.add(injectionPoint);
      return bean;
   }

   private static class DummyContext implements Context
   {

      public <T> T get(Contextual<T> contextual)
      {
         return null;
      }

      public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
      {
         return null;
      }

      public Class<? extends Annotation> getScope()
      {
         return Dummy.class;
      }

      public boolean isActive()
      {
         return false;
      }

   }

   @Inject
   private BeanManagerImpl beanManager;

   @Test
   public void testBeanBelongingToParentActivityBelongsToChildActivity()
   {
      Assert.assertEquals(1, beanManager.getBeans(Cow.class).size());
      Contextual<?> bean = beanManager.getBeans(Cow.class).iterator().next();
      BeanManager childActivity = beanManager.createActivity();
      Assert.assertEquals(1, childActivity.getBeans(Cow.class).size());
      Assert.assertEquals(bean, childActivity.getBeans(Cow.class).iterator().next());
   }

   @Test
   public void testBeanBelongingToParentActivityCanBeInjectedIntoChildActivityBean()
   {
      Assert.assertEquals(1, beanManager.getBeans(Cow.class).size());
      BeanManagerImpl childActivity = beanManager.createActivity();
      Bean<?> dummyBean = createDummyBean(childActivity, Cow.class);
      childActivity.addBean(dummyBean);
      Assert.assertNotNull(
            childActivity.getInjectableReference(
                  dummyBean.getInjectionPoints().iterator().next(),
                  childActivity.createCreationalContext(dummyBean)));
   }

   @Test
   public void testObserverBelongingToParentActivityBelongsToChildActivity()
   {
      Assert.assertEquals(1, beanManager.resolveObserverMethods(new NightTime()).size());
      ObserverMethod<?> observer = beanManager.resolveObserverMethods(new NightTime()).iterator().next();
      BeanManager childActivity = beanManager.createActivity();
      Assert.assertEquals(1, childActivity.resolveObserverMethods(new NightTime()).size());
      Assert.assertEquals(observer, childActivity.resolveObserverMethods(new NightTime()).iterator().next());
   }

   @Test
   public void testObserverBelongingToParentFiresForChildActivity()
   {
      Fox.setObserved(false);
      BeanManager childActivity = beanManager.createActivity();
      childActivity.fireEvent(new NightTime());
      Assert.assertTrue(Fox.isObserved());
   }

   @Test
   public void testContextObjectBelongingToParentBelongsToChild()
   {
      Context context = new DummyContext()
      {

         @Override
         public boolean isActive()
         {
            return true;
         }

      };
      beanManager.addContext(context);
      BeanManager childActivity = beanManager.createActivity();
      Assert.assertNotNull(childActivity.getContext(Dummy.class));
   }

   @Test
   public void testBeanBelongingToChildActivityCannotBeInjectedIntoParentActivityBean()
   {
      Assert.assertEquals(1, beanManager.getBeans(Cow.class).size());
      BeanManagerImpl childActivity = beanManager.createActivity();
      Bean<?> dummyBean = createDummyBean(childActivity, Cow.class);
      childActivity.addBean(dummyBean);
      Assert.assertEquals(0, beanManager.getBeans(Object.class, new AnnotationLiteral<Tame>()
      {
      }).size());
   }

   @Test(expected = UnsatisfiedResolutionException.class)
   public void testInstanceProcessedByParentActivity()
   {
      Context dummyContext = new DummyContext();
      beanManager.addContext(dummyContext);
      Assert.assertEquals(1, beanManager.getBeans(Cow.class).size());
      final Bean<Cow> bean = (Bean<Cow>)beanManager.getBeans(Cow.class).iterator().next();
      BeanManagerImpl childActivity = beanManager.createActivity();
      final Set<Annotation> bindingTypes = new HashSet<Annotation>();
      bindingTypes.add(new AnnotationLiteral<Tame>()
      {
      });
      childActivity.addBean(new ForwardingBean<Cow>()
      {

         @Override
         protected Bean<Cow> delegate()
         {
            return bean;
         }

         @Override
         public Set<Annotation> getQualifiers()
         {
            return bindingTypes;
         }

         @Override
         public Set<Class<? extends Annotation>> getStereotypes()
         {
            return Collections.emptySet();
         }

      });
      Utils.getReference(beanManager, Field.class).get();
   }

   @Test
   public void testObserverBelongingToChildDoesNotFireForParentActivity()
   {
      BeanManagerImpl childActivity = beanManager.createActivity();
      ObserverMethod<NightTime> observer = new ObserverMethod<NightTime>()
      {

         public void notify(NightTime event)
         {
            assert false;
         }

         public Class<?> getBeanClass()
         {
            return NightTime.class;
         }

         public Set<Annotation> getObservedQualifiers()
         {
            return Arrays2.asSet(AnyLiteral.INSTANCE, DefaultLiteral.INSTANCE);
         }

         public Type getObservedType()
         {
            return NightTime.class;
         }

         public Reception getReception()
         {
            return Reception.ALWAYS;
         }

         public TransactionPhase getTransactionPhase()
         {
            return TransactionPhase.IN_PROGRESS;
         }

      };
      // TODO Fix this test to use an observer method in a child activity
      childActivity.addObserver(observer);
      beanManager.fireEvent(new NightTime());
   }

}
