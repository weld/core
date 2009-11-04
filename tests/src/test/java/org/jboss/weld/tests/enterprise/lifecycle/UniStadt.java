package org.jboss.weld.tests.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface UniStadt
{
   public void removeBean();
}
