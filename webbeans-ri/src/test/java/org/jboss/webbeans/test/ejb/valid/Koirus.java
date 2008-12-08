package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Dependent;

@Stateful
@Dependent
public class Koirus
{
   @Remove
   public void bye(Object param) {
   }
}