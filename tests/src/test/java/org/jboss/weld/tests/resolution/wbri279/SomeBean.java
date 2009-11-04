package org.jboss.weld.tests.resolution.wbri279;

import javax.inject.Inject;

public class SomeBean
{

   @Inject @IntFactory
   IntegerFactory integerFactory;

   public String getObjectAsString()
   {
      return integerFactory.createObject().toString();
   }

}
