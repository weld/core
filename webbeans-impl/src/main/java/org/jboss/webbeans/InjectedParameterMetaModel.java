package org.jboss.webbeans;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.bindings.CurrentBinding;

public class InjectedParameterMetaModel extends AbstractInjectedThingMetaModel
{
   
   private static Annotation[] currentBinding = {new CurrentBinding()};
   
   private Class<?> type;
   
   public InjectedParameterMetaModel(Annotation[] bindingTypes, Class<?> type)
   {
      super(bindingTypes);
      this.type = type;
   }

   public InjectedParameterMetaModel(Class<?> type)
   {
      super(currentBinding);
      this.type = type;
   }

   public Class<?> getType()
   {
      return type;
   }
   
}
