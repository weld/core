package org.jboss.weld.test.activities.current;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

class Donkey
{
   
   BeanManager beanManager;
   
   public Donkey() throws NamingException
   {
      beanManager = (BeanManager) new InitialContext().lookup("java:app/BeanManager");
   }
   
   public BeanManager getManager()
   {
      return beanManager;
   }
   
}
