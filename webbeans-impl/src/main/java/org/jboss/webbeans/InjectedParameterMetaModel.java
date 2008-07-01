package org.jboss.webbeans;

import java.lang.annotation.Annotation;

public class InjectedParameterMetaModel extends AbstractInjectedThingMetaModel
{
   
   private Class<?> type;
   
   public InjectedParameterMetaModel(Annotation[] bindingTypes, Class<?> type)
   {
      super(bindingTypes);
      this.type = type;
   }

   public InjectedParameterMetaModel(Class<?> type)
   {
      super();
      this.type = type;
   }

   public Class<?> getType()
   {
      return type;
   }
   
}
