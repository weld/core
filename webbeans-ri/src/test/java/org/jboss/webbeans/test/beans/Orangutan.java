package org.jboss.webbeans.test.beans;

import javax.ejb.Remote;
import javax.webbeans.Destructor;
import javax.webbeans.Named;
import javax.webbeans.Production;

@Remote
@Production
//TODO @BoundTo("/beans/orangutan")
@Named
public interface Orangutan extends Animal
{
   
   @Destructor
   public void removeOrangutan();

}
