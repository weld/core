package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;

@Stateful
@RequestScoped
public class FrankfurtAmMain implements GrossStadt
{

   private boolean kleinStadtCreated = false;
   private boolean kleinStadtDestroyed = false;
   
   public boolean isKleinStadtCreated()
   {
      return kleinStadtCreated;
   }

   public boolean isKleinStadtDestroyed()
   {
      return kleinStadtDestroyed;
   }

   public void kleinStadtCreated()
   {
      kleinStadtCreated = true;
   }

   public void kleinStadtDestroyed()
   {
      kleinStadtDestroyed = true;
   }

   public void resetCreatedFlags()
   {
      kleinStadtCreated = false;
   }

   public void resetDestroyedFlags()
   {
      kleinStadtDestroyed = false;
   }

   @Remove
   public void dispose()
   {
   }

   private boolean schlossDestroyed = false;

   public boolean isSchlossDestroyed()
   {
      return schlossDestroyed;
   }

   public void schlossDestroyed()
   {
      schlossDestroyed = true;
   }

}
