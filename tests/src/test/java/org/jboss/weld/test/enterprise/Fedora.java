package org.jboss.weld.test.enterprise;

import javax.ejb.Local;

@Local
public interface Fedora
{
   
   public void causeRuntimeException();
   

}
