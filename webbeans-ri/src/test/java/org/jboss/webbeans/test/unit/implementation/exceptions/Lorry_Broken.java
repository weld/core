package org.jboss.webbeans.test.unit.implementation.exceptions;

class Lorry_Broken
{
   
   public Lorry_Broken() throws Exception
   {
      throw new FooException();
   }
   
}
