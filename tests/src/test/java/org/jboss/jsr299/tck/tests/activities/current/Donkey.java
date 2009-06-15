package org.jboss.jsr299.tck.tests.activities.current;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

class Donkey
{
   
   BeanManager beanManager;
   
   public Donkey() throws NamingException
   {
      beanManager = (BeanManager) new InitialContext().lookup("java:app/Manager");
   }
   
   public BeanManager getManager()
   {
      return beanManager;
   }
   
}
