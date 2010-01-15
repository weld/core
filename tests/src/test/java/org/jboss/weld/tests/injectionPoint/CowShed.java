package org.jboss.weld.tests.injectionPoint;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.FieldInjectionPoint;

public class CowShed
{

   @Produces
   public Cow get(InjectionPoint ip)
   {
      assert ip instanceof FieldInjectionPoint<?, ?>;
      FieldInjectionPoint<?, ?> fip = (FieldInjectionPoint<?, ?>) ip;
      assert fip.getDeclaringType().getJavaClass().equals(Field.class);
      return new Cow("daisy");
   }
   
}
