package org.jboss.webbeans.transaction;

import javax.ejb.Local;

@Local
public interface LocalTransactionListener 
{
   public void destroy();
}
