package org.jboss.weld.tests.enterprise;

import javax.ejb.Local;

@Local
public interface Scottish
{

   public Feed getFeed();
   
}
