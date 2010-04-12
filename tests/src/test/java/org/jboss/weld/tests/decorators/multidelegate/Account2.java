package org.jboss.weld.tests.decorators.multidelegate;

import java.math.BigDecimal;

public class Account2 implements Account {

   public String withdraw(BigDecimal amount) {
      return "Account2 withdraw";
   }
   
}