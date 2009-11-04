package org.jboss.weld.tests.extensions;

import javax.inject.Inject;

public class Foo
{
   
   @Inject SimpleExtension simpleExtension;
   
   public SimpleExtension getSimpleExtension()
   {
      return simpleExtension;
   }

}
