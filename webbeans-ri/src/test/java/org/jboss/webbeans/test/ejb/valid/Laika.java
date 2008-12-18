package org.jboss.webbeans.test.ejb.valid;

import javax.ejb.Remove;
import javax.webbeans.ApplicationScoped;

import org.jboss.webbeans.test.annotations.Singleton;

@Singleton
@ApplicationScoped
public class Laika
{

   @Remove
   public void remove()
   {
      
   }
   
}
