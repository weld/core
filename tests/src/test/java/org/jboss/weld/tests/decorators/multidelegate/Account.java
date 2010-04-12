package org.jboss.weld.tests.decorators.multidelegate;

import java.io.Serializable;
import java.math.BigDecimal;

public interface Account extends Serializable 
{
   
   public String withdraw(BigDecimal amount);

}
