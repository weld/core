package org.jboss.weld.tests.exceptions;

class Lorry_Broken
{
   
   public Lorry_Broken() throws Exception
   {
      throw new FooException();
   }
   
}
