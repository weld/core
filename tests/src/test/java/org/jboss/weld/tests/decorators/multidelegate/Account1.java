package org.jboss.weld.tests.decorators.multidelegate;

import java.math.BigDecimal;

public class Account1 implements Account {

   public String withdraw(BigDecimal amount) {
      return "Account1 withdraw";
   }
   
}