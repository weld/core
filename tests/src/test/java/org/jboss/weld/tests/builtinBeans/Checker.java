package org.jboss.weld.tests.builtinBeans;

import java.security.Principal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class Checker
{
   
   public static boolean checkPrincipal(Principal principal)
   {
      principal.getName();
      return true;
   }
   
   public static boolean checkBeanManager(BeanManager beanManager)
   {
      return beanManager != null && beanManager.isScope(ApplicationScoped.class);
   }
   
   public static boolean checkUserTransaction(UserTransaction userTransaction)
   {
      try
      {
         if (userTransaction != null)
         {
            userTransaction.getStatus();
            return true;
         }
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
      return false;
   }
   
   public static boolean checkValidator(Validator validator)
   {
      try
      {
         if (validator != null)
         {
            validator.unwrap(String.class);
         }
      }
      catch (ValidationException e)
      {
         return true;
      }
      return false;
   }

   public static boolean checkValidatorFactory(ValidatorFactory validatorFactory)
   {
      try
      {
         if (validatorFactory != null)
         {
            validatorFactory.unwrap(String.class);
         }
      }
      catch (ValidationException e)
      {
         return true;
      }
      return false;
   }
   
   public static boolean checkInstance(Instance<Cow> cow)
   {
      if (cow != null && cow.get() != null)
      {
         return cow.get().getName().equals("Daisy");
      }
      else
      {
         return false;
      }
   }
   
   public static boolean checkEvent(Event<Cow> cowEvent, CowEventObserver observer)
   {
      observer.reset();
      if (cowEvent != null)
      {
         cowEvent.fire(new Cow());
         return observer.isObserved(); 
      }
      else
      {
         return false;
      }
   }

   public static boolean checkInjectionPoint(InjectionPoint injectionPoint, Class<?> injectedClass)
   {
      if (injectionPoint != null)
      {
         return injectedClass.equals(injectionPoint.getBean().getBeanClass());
      }
      else
      {
         return false;
      }
   }
   
   public static boolean checkEquality(Object object1, Object object2)
   {
      return object1.equals(object2) && object1.hashCode() == object2.hashCode();
   }
   
}
