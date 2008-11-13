package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Map;

import javax.webbeans.BindingType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.util.Reflections;

public abstract class AbstractAnnotatedMember<T, S extends Member> extends AbstractAnnotatedItem<T, S>
{
   
   private String name;

   public AbstractAnnotatedMember(Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(annotationMap);
   }
   
   public boolean isStatic()
   {
      return Reflections.isStatic(getDelegate());
   }
   
   public boolean isFinal()
   {
      return Reflections.isFinal(getDelegate());
   }
   
   public T getValue(ManagerImpl manager)
   {
      return manager.getInstanceByType(getType(), getAnnotationsAsArray(BindingType.class));
   }
   
   public String getName()
   {
      if (name == null)
      {
         name = getDelegate().getName();
      }
      return name;
   }
   
}
