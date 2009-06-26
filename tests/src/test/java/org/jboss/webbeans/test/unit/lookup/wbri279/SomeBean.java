package org.jboss.webbeans.test.unit.lookup.wbri279;

public class SomeBean
{

   @IntFactory
   IntegerFactory integerFactory;

   public String getObjectAsString()
   {
      return integerFactory.createObject().toString();
   }

}
