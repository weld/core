package org.jboss.weld.tests.enterprise;

import javax.inject.Inject;

public class ResultClient
{
   
   @Inject @DAO ResultDAO result;
   
   public Result lookupPete()
   {
      return result.findByUser("pete");
   }

}
