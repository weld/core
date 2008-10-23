package org.jboss.webbeans.test.util;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;

/**
 * An empty annotated type
 * 
 * TODO Update testsuite not to need this hack
 * 
 * @author pmuir
 *
 */
public class EmptyAnnotatedType<T> extends SimpleAnnotatedType<T>
{
   
   public EmptyAnnotatedType(Map<Class<? extends Annotation>, Annotation> annotationMap)
   {
      super(null, annotationMap);
   }

   public Set<AnnotatedField<?>> getFields()
   {
      return null;
   }
   
   @Override
   public Set<AnnotatedField<?>> getMetaAnnotatedFields(
         Class<? extends Annotation> metaAnnotationType)
   {
      return null;
   }

}