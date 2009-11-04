package org.jboss.weld.test.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface Schloss
{
   public void destructionCallback();
   
   public void remove();
}
