package org.jboss.webbeans.transaction;

import javax.ejb.Local;

@Local
public interface LocalEjbSynchronizations extends Synchronizations
{
   public void destroy();
}
