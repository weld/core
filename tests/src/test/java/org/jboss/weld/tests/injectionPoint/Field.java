package org.jboss.weld.tests.injectionPoint;

import javax.inject.Inject;

public class Field
{
   
   @Inject Cow cow;
   
   public Cow getCow()
   {
      return cow;
   }

}
