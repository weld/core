package org.jboss.webbeans.test.unit.implementation.enterprise.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;

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
