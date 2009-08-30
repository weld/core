package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

import javax.inject.Inject;

public class ParameterizedListInjection
{

   private List<String> value;

   @Inject
   private List<String> fieldInjection;

   private List<String> setterInjection;

   @Inject
   public void init(List<String> setterInjection)
   {
      this.setterInjection = setterInjection;
   }

   @Inject
   public ParameterizedListInjection(List<String> com)
   {
      this.value = com;
   }

   public java.util.List<String> getValue()
   {
      return value;
   }

   public List<String> getFieldInjection()
   {
      return fieldInjection;
   }

   public List<String> getSetterInjection()
   {
      return setterInjection;
   }

}
