package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;

public class Parameter<T> extends Element<T>
{
   
   private static Annotation[] currentBinding = {new CurrentAnnotationLiteral()};
   
   private Class<? extends T> type;
   
   public Parameter(Annotation[] bindingTypes, Class<? extends T> type)
   {
      super(bindingTypes);
      this.type = type;
   }

   public Parameter(Class<? extends T> type)
   {
      super(currentBinding);
      this.type = type;
   }

   public Class<? extends T> getType()
   {
      return type;
   }
   
   
   
}
