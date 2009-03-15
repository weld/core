package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

public class ParameterizedListInjection
{

   private List<String> value;

   //@Current
   private List<String> fieldInjection;

   private List<String> setterInjection;

   //@Initializer
   public void init(List<String> setterInjection)
   {
      this.setterInjection = setterInjection;
   }

   /*@Initializer
   public ParameterizedListInjection(List<String> com)
   {
      this.value = com;
   }*/

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
