package org.jboss.webbeans.test.unit.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface Schloss
{
   public void destructionCallback();
   
   public void remove();
}
