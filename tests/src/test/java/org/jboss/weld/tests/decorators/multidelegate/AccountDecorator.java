package org.jboss.weld.tests.decorators.multidelegate;

import java.math.BigDecimal;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

@Decorator
public class AccountDecorator implements Account {
   
   @Inject @Delegate @Any Account account;

   public String withdraw(BigDecimal amount) {
      System.out.println("AccountDecorator withdraw " + account.toString());
      return account.withdraw(amount);
   }

}