package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.jboss.webbeans.introspector.AnnotatedParameter;

public class SimpleAnnotatedParameter<T> extends AbstractAnnotatedItem<T, Object> implements AnnotatedParameter<T>
{

   private Class<T> type;
   private Type[] actualTypeArguments = new Type[0];
   private boolean _final;
   private boolean _static;

   public SimpleAnnotatedParameter(Annotation[] annotations, Class<T> type)
   {
      super(buildAnnotationMap(annotations));
      this.type = type;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   public Object getDelegate()
   {
      return null;
   }

   public Class<T> getType()
   {
      return type;
   }

   public boolean isFinal()
   {
      return _final;
   }

   public boolean isStatic()
   {
      return _static;
   }
   

}
