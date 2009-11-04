package org.jboss.weld.tests.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface Schloss
{
   public void destructionCallback();
   
   public void remove();
}
