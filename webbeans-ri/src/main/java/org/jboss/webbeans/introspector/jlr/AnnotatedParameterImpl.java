package org.jboss.webbeans.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.webbeans.BindingType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class AnnotatedParameterImpl<T> extends AbstractAnnotatedItem<T, Object> implements AnnotatedParameter<T>
{

   private Class<T> type;
   private Type[] actualTypeArguments = new Type[0];
   private boolean _final;
   private boolean _static;

   public AnnotatedParameterImpl(Annotation[] annotations, Class<T> type)
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
   
   public T getValue(ManagerImpl manager)
   {
      return manager.getInstanceByType(getType(), getMetaAnnotationsAsArray(BindingType.class));
   }

   public String getName()
   {
      throw new IllegalArgumentException("Unable to determine name of parameter");
   }
   
}
