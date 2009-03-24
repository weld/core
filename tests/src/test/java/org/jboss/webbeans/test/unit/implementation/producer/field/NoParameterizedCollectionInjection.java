package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.Collection;

import javax.inject.Current;
import javax.inject.Initializer;

public class NoParameterizedCollectionInjection
{

   private Collection value;

   @Current
   private Collection fieldInjection;

   private Collection setterInjection;

   @Initializer
   public void init(Collection setterInjection)
   {
      this.setterInjection = setterInjection;
   }

   @Initializer
   public NoParameterizedCollectionInjection(Collection com)
   {
      this.value = com;
   }

   public Collection getValue()
   {
      return value;
   }

   public Collection getFieldInjection()
   {
      return fieldInjection;
   }

   public Collection getSetterInjection()
   {
      return setterInjection;
   }

}
