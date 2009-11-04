package org.jboss.weld.tests.enterprise;

import javax.ejb.Local;

@Local
public interface Fedora
{
   
   public void causeRuntimeException();
   

}
