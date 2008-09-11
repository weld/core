package org.jboss.webbeans.test.components;

import javax.ejb.Remote;
import javax.webbeans.BoundTo;
import javax.webbeans.Destroys;
import javax.webbeans.Named;
import javax.webbeans.Production;

@Remote
@Production
@BoundTo("/beans/orangutan")
@Named
public interface Orangutan extends Animal
{
   
   @Destroys
   public void removeOrangutan();

}
