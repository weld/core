package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface Schloss
{
   public void destructionCallback();
   
   public void remove();
}
