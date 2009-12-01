package org.jboss.weld.tests.injectionPoint;

import javax.inject.Inject;

public class StringConsumer
{

   @Inject String str;
   
   public void ping() {}
   
}
