package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedParameter;

public class InjectableParameter<T> extends Injectable<T, Object>
{
   
   private static Annotation[] currentBinding = {new CurrentAnnotationLiteral()};
   
   protected InjectableParameter() {}
   
   public InjectableParameter(Annotation[] bindingTypes, Class<? extends T> type)
   {
      super(new SimpleAnnotatedParameter<T>(bindingTypes, type));
   }
   
   public InjectableParameter(AnnotatedParameter<T> annotatedParameter)
   {
      super(annotatedParameter);
   }

   public InjectableParameter(Class<? extends T> type)
   {
      this(currentBinding, type);
   }
   
}
