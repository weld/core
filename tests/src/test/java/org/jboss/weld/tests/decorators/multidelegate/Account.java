package org.jboss.weld.tests.decorators.multidelegate;

import java.math.BigDecimal;

public interface Account {
   
   public String withdraw(BigDecimal amount);

}