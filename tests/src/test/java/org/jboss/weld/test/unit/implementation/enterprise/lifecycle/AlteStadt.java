package org.jboss.weld.test.unit.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface AlteStadt
{
   public String getPlaceOfInterest();
   
   public void performPostConstructChecks();
   
   public void initializeBean(GutenbergMuseum pointOfInterest);
   
   public GutenbergMuseum getAnotherPlaceOfInterest();
}
