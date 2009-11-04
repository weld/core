package org.jboss.weld.test.extensions;

import javax.inject.Inject;

public class Foo
{
   
   @Inject SimpleExtension simpleExtension;
   
   public SimpleExtension getSimpleExtension()
   {
      return simpleExtension;
   }

}
