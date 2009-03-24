package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.Collection;

import javax.inject.Current;
import javax.inject.Initializer;

public class IntegerCollectionInjection
{

   private Collection<Integer> value;

   @Current
   private Collection<Integer> fieldInjection;

   private Collection<Integer> setterInjection;

   @Initializer
   public void init(Collection<Integer> setterInjection)
   {
      this.setterInjection = setterInjection;
   }

   @Initializer
   public IntegerCollectionInjection(Collection<Integer> com)
   {
      this.value = com;
   }

   public Collection<Integer> getValue()
   {
      return value;
   }

   public Collection<Integer> getFieldInjection()
   {
      return fieldInjection;
   }

   public Collection<Integer> getSetterInjection()
   {
      return setterInjection;
   }

}
