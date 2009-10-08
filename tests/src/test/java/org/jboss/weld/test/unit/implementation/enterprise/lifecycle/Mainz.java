package org.jboss.weld.test.unit.implementation.enterprise.lifecycle;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class Mainz implements AlteStadt
{
   @Inject
   private RoemerPassage placeOfInterest;
   
   private GutenbergMuseum anotherPlaceOfInterest;
   
   private String name;

   public String getPlaceOfInterest()
   {
      return name;
   }

   @PostConstruct
   public void performPostConstructChecks()
   {
      if ( placeOfInterest != null )
         name = placeOfInterest.getName();
   }
   
   @Inject
   public void initializeBean(GutenbergMuseum pointOfInterest)
   {
      this.anotherPlaceOfInterest = pointOfInterest;
   }

   public GutenbergMuseum getAnotherPlaceOfInterest()
   {
      return anotherPlaceOfInterest;
   }
}
