package org.jboss.weld.test.exceptions;

class Lorry_Broken
{
   
   public Lorry_Broken() throws Exception
   {
      throw new FooException();
   }
   
}
