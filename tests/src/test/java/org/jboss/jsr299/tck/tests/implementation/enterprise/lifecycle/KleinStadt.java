package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface KleinStadt
{
   public void begruendet();
   
   public void zustandVergessen();
   
   public void zustandVerloren();
   
}
