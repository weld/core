package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface GrossStadt
{
   public void kleinStadtCreated();
   
   public void kleinStadtDestroyed();
   
   public boolean isKleinStadtCreated();
   
   public boolean isKleinStadtDestroyed();
   
   public void resetCreatedFlags();
   
   public void resetDestroyedFlags();
   
   public void dispose();

   public void schlossDestroyed();

   public boolean isSchlossDestroyed();
}
