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
import org.testng.annotations.Test;

@Artifact
public class BuiltInBeanPassivationCapableTest extends AbstractWeldTest
{

   @Test
   public void testDefaultValidatorBean() throws Throwable
   {
      Validator validator = getCurrentManager().getInstanceByType(Validator.class);
      Validator validator1 = deserialize(serialize(validator));
      assert checkValidator(validator1);
   }

   @Test
   public void testDefaultValidatorFactoryBean() throws Throwable
   {
      ValidatorFactory validatorFactory = getCurrentManager().getInstanceByType(ValidatorFactory.class);
      ValidatorFactory validatorFactory1 = deserialize(serialize(validatorFactory));
      assert checkValidatorFactory(validatorFactory1);
   }

   @Test(groups="incontainer-broken")
   public void testPrincipal() throws Throwable
   {
      Principal principal = getCurrentManager().getInstanceByType(Principal.class);
      Principal principal1 = deserialize(serialize(principal));
      assert checkPrincipal(principal1);
   }

   @Test
   public void testUserTransactionBean() throws Throwable
   {
      UserTransaction userTransaction = getCurrentManager().getInstanceByType(UserTransaction.class);
      UserTransaction userTransaction1 = deserialize(serialize(userTransaction));
      assert checkUserTransaction(userTransaction1);
   }
   
   @Test 
   public void testBeanManagerBean() throws Throwable
   {
      BeanManager beanManager = getCurrentManager().getInstanceByType(BeanManager.class);
      BeanManager beanManager1 = deserialize(serialize(beanManager));
      assert checkBeanManager(beanManager1);
      assert checkEquality(beanManager, beanManager1);
   }
   
   @Test
   public void testInstance() throws Throwable
   {
      Instance<Cow> instance = getCurrentManager().getInstanceByType(Consumer.class).getCow();
      Instance<Cow> instance1 = deserialize(serialize(instance));
      assert checkInstance(instance1);
      assert checkEquality(instance, instance1);
   }
   
   @Test
   public void testEvent() throws Throwable
   {
      Event<Cow> event = getCurrentManager().getInstanceByType(Consumer.class).getEvent();
      CowEventObserver observer = getCurrentManager().getInstanceByType(CowEventObserver.class);
      Event<Cow> event1 = deserialize(serialize(event));
      assert checkEvent(event1, observer);
      assert checkEquality(event, event1);
   }
   
   @Test
   public void testFieldInjectionPoint() throws Throwable
   {
      Dog.reset();
      getCurrentManager().getInstanceByType(FieldInjectionPointConsumer.class).ping();
      InjectionPoint injectionPoint = Dog.getInjectionPoint();
      InjectionPoint injectionPoint1 = deserialize(serialize(injectionPoint));
      assert checkInjectionPoint(injectionPoint1, FieldInjectionPointConsumer.class);
      assert checkEquality(injectionPoint, injectionPoint1);
   }
   
   @Test
   public void testConstructorInjectionPoint() throws Throwable
   {
      Dog.reset();
      getCurrentManager().getInstanceByType(ConstructorInjectionPointConsumer.class).ping();
      InjectionPoint injectionPoint = Dog.getInjectionPoint();
      InjectionPoint injectionPoint1 = deserialize(serialize(injectionPoint));
      assert checkInjectionPoint(injectionPoint1, ConstructorInjectionPointConsumer.class);
      assert checkEquality(injectionPoint, injectionPoint1);
   }
   
   @Test
   public void testMethodInjectionPoint() throws Throwable
   {
      Dog.reset();
      getCurrentManager().getInstanceByType(MethodInjectionPointConsumer.class).ping();
      InjectionPoint injectionPoint = Dog.getInjectionPoint();
      InjectionPoint injectionPoint1 = deserialize(serialize(injectionPoint));
      assert checkInjectionPoint(injectionPoint1, MethodInjectionPointConsumer.class);
      assert checkEquality(injectionPoint, injectionPoint1);
   }
   
   @Test
   public void testAllOnBean() throws Throwable
   {
      Consumer consumer = getCurrentManager().getInstanceByType(Consumer.class);
      consumer.check();
      Consumer consumer1 = deserialize(serialize(consumer));
      consumer1.check();
   }

}
