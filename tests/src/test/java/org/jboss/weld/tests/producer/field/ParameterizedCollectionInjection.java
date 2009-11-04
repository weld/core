package org.jboss.weld.tests.producer.field;

import java.util.Collection;

import javax.inject.Inject;

public class ParameterizedCollectionInjection
{

   private Collection<String> value;

   @Inject
   private Collection<String> fieldInjection;

   private Collection<String> setterInjection;

   @Inject
   public void init(Collection<String> setterInjection)
   {
      this.setterInjection = setterInjection;
   }

   @Inject
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
