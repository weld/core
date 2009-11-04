package org.jboss.weld.test.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface UniStadt
{
   public void removeBean();
}
