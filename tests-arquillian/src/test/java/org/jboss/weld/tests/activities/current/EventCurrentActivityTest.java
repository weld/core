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
package org.jboss.weld.tests.activities.current;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.jboss.weld.tests.category.Broken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * Spec version: 20090519
 *
 */
@RunWith(Arquillian.class)
public class EventCurrentActivityTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(EventCurrentActivityTest.class.getPackage())
         .addClasses(Utils.class);
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
         return true;
      }

   }

   @Inject
   private BeanManagerImpl beanManager;

   @Test
   @Category(Broken.class)
   public void testEventProcessedByCurrentActivity()
   {
      DummyContext dummyContext = new DummyContext();
      beanManager.addContext(dummyContext);
      BeanManagerImpl childActivity = beanManager.createActivity();
      TestableObserverMethod<NightTime> observer = new TestableObserverMethod<NightTime>()
      {

         boolean observed = false;

         public void notify(NightTime event)
         {
            observed = true;
         }

         public boolean isObserved()
         {
            return observed;
         }

         public Class<?> getBeanClass()
         {
            return NightTime.class;
         }

         public Set<Annotation> getObservedQualifiers()
         {
            return Collections.<Annotation>singleton(AnyLiteral.INSTANCE);
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
      childActivity.addObserver(observer);
      childActivity.setCurrent(dummyContext.getScope());
      Utils.getReference(beanManager, Dusk.class).ping();
      Assert.assertTrue(observer.isObserved());
   }
}
