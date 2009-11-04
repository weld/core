package org.jboss.weld.test.enterprise.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@Stateful
@RequestScoped
public class Kassel implements KleinStadt, SchoeneStadt
{
   @Inject
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
