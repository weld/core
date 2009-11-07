package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@SessionScoped
public class Consumer implements Serializable
{
   
   @Inject Validator validator;
   @Inject ValidatorFactory validatorFactory;
   // Not working incontainer as there is no principal
   //@Inject Principal principal;
   @Inject UserTransaction userTransaction;
   @Inject BeanManager beanManager;
   @Inject Instance<Cow> cow;
   @Inject Event<Cow> event;
   @Inject CowEventObserver observer;
   
   @PostConstruct
   public void postConstruct()
   {
      cow.get().setName("Daisy");
   }
   
   public Instance<Cow> getCow()
   {
      return cow;
   }
   
   public Event<Cow> getEvent()
   {
      return event;
   }
   
   public void check()
   {
      assert Checker.checkBeanManager(beanManager);

      // Not working incontainer as there is no principal
      //assert Checker.checkPrincipal(principal);
      assert Checker.checkUserTransaction(userTransaction);
      assert Checker.checkValidator(validator);
      assert Checker.checkValidatorFactory(validatorFactory);
      assert Checker.checkInstance(cow);
      assert Checker.checkEvent(event, observer);
   }
   
   
   
}
