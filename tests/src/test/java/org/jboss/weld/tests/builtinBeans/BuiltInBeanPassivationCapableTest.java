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
package org.jboss.weld.tests.builtinBeans;

import static org.jboss.weld.tests.builtinBeans.Checker.checkBeanManager;
import static org.jboss.weld.tests.builtinBeans.Checker.checkEquality;
import static org.jboss.weld.tests.builtinBeans.Checker.checkEvent;
import static org.jboss.weld.tests.builtinBeans.Checker.checkInjectionPoint;
import static org.jboss.weld.tests.builtinBeans.Checker.checkInstance;
import static org.jboss.weld.tests.builtinBeans.Checker.checkPrincipal;
import static org.jboss.weld.tests.builtinBeans.Checker.checkUserTransaction;
import static org.jboss.weld.tests.builtinBeans.Checker.checkValidator;
import static org.jboss.weld.tests.builtinBeans.Checker.checkValidatorFactory;

import java.security.Principal;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.test.Utils;
import org.testng.annotations.Test;

@Artifact
public class BuiltInBeanPassivationCapableTest extends AbstractWeldTest
{

   @Test
   public void testDefaultValidatorBean() throws Throwable
   {
      Validator validator = getReference(Validator.class);
      Validator validator1 = Utils.deserialize(Utils.serialize(validator));
      assert checkValidator(validator1);
   }

   @Test
   public void testDefaultValidatorFactoryBean() throws Throwable
   {
      ValidatorFactory validatorFactory = getReference(ValidatorFactory.class);
      ValidatorFactory validatorFactory1 = Utils.deserialize(Utils.serialize(validatorFactory));
      assert checkValidatorFactory(validatorFactory1);
   }

   @Test(groups="incontainer-broken")
   public void testPrincipal() throws Throwable
   {
      Principal principal = getReference(Principal.class);
      Principal principal1 = Utils.deserialize(Utils.serialize(principal));
      assert checkPrincipal(principal1);
   }

   @Test
   public void testUserTransactionBean() throws Throwable
   {
      UserTransaction userTransaction = getReference(UserTransaction.class);
      UserTransaction userTransaction1 = Utils.deserialize(Utils.serialize(userTransaction));
      assert checkUserTransaction(userTransaction1);
   }
   
   @Test 
   public void testBeanManagerBean() throws Throwable
   {
      BeanManager beanManager = getReference(BeanManager.class);
      BeanManager beanManager1 = Utils.deserialize(Utils.serialize(beanManager));
      assert checkBeanManager(beanManager1);
      assert checkEquality(beanManager, beanManager1);
   }
   
   @Test
   public void testInstance() throws Throwable
   {
      Instance<Cow> instance = getReference(Consumer.class).getCow();
      Instance<Cow> instance1 = Utils.deserialize(Utils.serialize(instance));
      assert checkInstance(instance1);
      assert checkEquality(instance, instance1);
   }
   
   @Test
   public void testEvent() throws Throwable
   {
      Event<Cow> event = getReference(Consumer.class).getEvent();
      CowEventObserver observer = getReference(CowEventObserver.class);
      Event<Cow> event1 = Utils.deserialize(Utils.serialize(event));
      assert checkEvent(event1, observer);
      assert checkEquality(event, event1);
   }
   
   @Test
   public void testFieldInjectionPoint() throws Throwable
   {
      Dog.reset();
      getReference(FieldInjectionPointConsumer.class).ping();
      InjectionPoint injectionPoint = Dog.getInjectionPoint();
      InjectionPoint injectionPoint1 = Utils.deserialize(Utils.serialize(injectionPoint));
      assert checkInjectionPoint(injectionPoint1, FieldInjectionPointConsumer.class);
      assert checkEquality(injectionPoint, injectionPoint1);
   }
   
   @Test
   public void testConstructorInjectionPoint() throws Throwable
   {
      Dog.reset();
      getReference(ConstructorInjectionPointConsumer.class).ping();
      InjectionPoint injectionPoint = Dog.getInjectionPoint();
      InjectionPoint injectionPoint1 = Utils.deserialize(Utils.serialize(injectionPoint));
      assert checkInjectionPoint(injectionPoint1, ConstructorInjectionPointConsumer.class);
      assert checkEquality(injectionPoint, injectionPoint1);
   }
   
   @Test
   public void testMethodInjectionPoint() throws Throwable
   {
      Dog.reset();
      getReference(MethodInjectionPointConsumer.class).ping();
      InjectionPoint injectionPoint = Dog.getInjectionPoint();
      InjectionPoint injectionPoint1 = Utils.deserialize(Utils.serialize(injectionPoint));
      assert checkInjectionPoint(injectionPoint1, MethodInjectionPointConsumer.class);
      assert checkEquality(injectionPoint, injectionPoint1);
   }
   
   @Test
   public void testAllOnBean() throws Throwable
   {
      Consumer consumer = getReference(Consumer.class);
      consumer.check();
      Consumer consumer1 = Utils.deserialize(Utils.serialize(consumer));
      consumer1.check();
   }

}
