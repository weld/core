package org.jboss.weld.test.unit.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface KleinStadt
{
   public void begruendet();
   
   public void zustandVergessen();
   
   public void zustandVerloren();
   
}
