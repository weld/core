package org.jboss.webbeans.introspector;

import java.lang.reflect.Method;

public class SimpleAnnotatedMethod extends AbstractAnnotatedItem<Method> implements AnnotatedMethod
{
   
   private Method annotatedMethod;
   
   public SimpleAnnotatedMethod(Method method)
   {
      super(buildAnnotationMap(method));
      this.annotatedMethod = method;
   }

   public Method getAnnotatedMethod()
   {
      return annotatedMethod;
   }
   
   @Override
   public String toString()
   {
      return annotatedMethod + " " + getAnnotatedMethod().toString();
   }

   public Method getDelegate()
   {
      return annotatedMethod;
   }

}
