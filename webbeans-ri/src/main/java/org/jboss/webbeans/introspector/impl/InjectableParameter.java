package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class InjectableParameter<T> extends Injectable<T, Object>
{
   
   private static Annotation[] currentBinding = {new CurrentAnnotationLiteral()};
   
   protected InjectableParameter() {}
   
   public InjectableParameter(Annotation[] bindingTypes, Class<T> type)
   {
      super(new SimpleAnnotatedParameter<T>(bindingTypes, type));
   }
   
   public InjectableParameter(AnnotatedParameter<T> annotatedParameter)
   {
      super(annotatedParameter);
   }

   public InjectableParameter(Class<T> type)
   {
      this(currentBinding, type);
   }
   
}
