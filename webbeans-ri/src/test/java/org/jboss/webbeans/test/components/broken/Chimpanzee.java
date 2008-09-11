package org.jboss.webbeans.test.components.broken;

import javax.ejb.Remote;
import javax.webbeans.BoundTo;
import javax.webbeans.Destroys;
import javax.webbeans.Production;

@Remote
@BoundTo("/beans/Chimpanzee")
@Production
public interface Chimpanzee
{

   @Destroys
   public void remove(String foo);
   
}
