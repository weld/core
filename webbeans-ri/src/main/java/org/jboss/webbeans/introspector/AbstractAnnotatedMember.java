package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Map;

import org.jboss.webbeans.util.Reflections;

public abstract class AbstractAnnotatedMember<T, S extends Member> extends AbstractAnnotatedItem<T, S>
{
   
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
   
}
