package org.jboss.weld.tests.resources;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

public class UTConsumer
{

   @Resource
   UserTransaction userTransaction;
   
   public UserTransaction getUserTransaction()
   {
      return userTransaction;
   }
   
}
