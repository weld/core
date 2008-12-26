package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Local;
import javax.ejb.Remove;

@Local
public interface LocalGoodDoggie
{
   @Remove
   public void bye();
}
