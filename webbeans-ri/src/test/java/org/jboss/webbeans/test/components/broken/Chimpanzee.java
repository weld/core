package org.jboss.webbeans.test.components.broken;

import javax.ejb.Remote;
import javax.webbeans.Destructor;
import javax.webbeans.Production;

@Remote
// TODO @BoundTo("/beans/Chimpanzee")
@Production
public interface Chimpanzee
{

   @Destructor
   public void remove(String foo);
   
}
