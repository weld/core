package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Member;

import javax.webbeans.BindingType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.util.Reflections;

public abstract class AbstractAnnotatedMember<T, S extends Member> extends AbstractAnnotatedItem<T, S>
{
   
   private String name;

   public AbstractAnnotatedMember(AnnotationMap annotationMap)
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
      return manager.getInstanceByType(getType(), getMetaAnnotationsAsArray(BindingType.class));
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
