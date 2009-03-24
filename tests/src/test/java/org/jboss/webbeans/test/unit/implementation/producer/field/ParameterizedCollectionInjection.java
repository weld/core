package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.Collection;

import javax.inject.Current;
import javax.inject.Initializer;

public class ParameterizedCollectionInjection
{

   private Collection<String> value;

   @Current
   private Collection<String> fieldInjection;

   private Collection<String> setterInjection;

   @Initializer
   public void init(Collection<String> setterInjection)
   {
      this.setterInjection = setterInjection;
   }

   @Initializer
   public ParameterizedCollectionInjection(Collection<String> com)
   {
      this.value = com;
   }

   public Collection<String> getValue()
   {
      return value;
   }

   public Collection<String> getFieldInjection()
   {
      return fieldInjection;
   }

   public Collection<String> getSetterInjection()
   {
      return setterInjection;
   }

}
