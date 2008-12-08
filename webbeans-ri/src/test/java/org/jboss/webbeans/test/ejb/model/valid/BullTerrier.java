package org.jboss.webbeans.test.ejb.model.valid;

import javax.ejb.Stateless;
import javax.webbeans.Observes;

import org.jboss.webbeans.test.annotations.Role;
import org.jboss.webbeans.test.annotations.Tame;

@Stateless
public class BullTerrier
{
   public void observesBadEvent(@Observes @Role("Admin") @Tame String someEvent)
   {
   }
}
