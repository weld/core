package org.jboss.weld.tests.enterprise;

import javax.ejb.Remote;

@Remote
public interface Bird
{

   public void observe(Feed feed);
   
}
