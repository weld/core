package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;

public class InjectableParameter<T> extends Injectable<T, Object>
{
   
   private static Annotation[] currentBinding = {new CurrentAnnotationLiteral()};
   
   protected InjectableParameter() {}
   
   public InjectableParameter(Annotation[] bindingTypes, Class<? extends T> type)
   {
      super(new SimpleAnnotatedItem<T, Object>(bindingTypes, type));
   }

   public InjectableParameter(Class<? extends T> type)
   {
      this(currentBinding, type);
   }
   
}
