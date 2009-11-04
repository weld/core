package org.jboss.weld.tests.producer.field;

import java.util.Collection;

import javax.inject.Inject;

public class IntegerCollectionInjection
{

   private Collection<Integer> value;

   @Inject
   private Collection<Integer> fieldInjection;

   private Collection<Integer> setterInjection;

   @Inject
   public void init(Collection<Integer> setterInjection)
   {
      this.setterInjection = setterInjection;
   }

   @Inject
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
