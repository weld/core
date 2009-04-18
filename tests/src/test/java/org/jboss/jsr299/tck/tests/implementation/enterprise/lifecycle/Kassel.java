package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.context.RequestScoped;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

@Stateful
@RequestScoped
public class Kassel implements KleinStadt, SchoeneStadt
{
   @EJB
   private GrossStadt grossStadt;
   
   @PostConstruct
   public void begruendet()
   {
      grossStadt.kleinStadtCreated();
   }

   @Remove
   public void zustandVergessen()
   {
   }

   @PreDestroy
   public void zustandVerloren()
   {
      grossStadt.kleinStadtDestroyed();
   }

}
