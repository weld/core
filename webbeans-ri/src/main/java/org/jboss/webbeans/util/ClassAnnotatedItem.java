package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of annotatedItem that wraps a simple Java class and reads
 * annotations from there.
 * 
 * @author pmuir
 *
 */
public class ClassAnnotatedItem extends AbstractAnnotatedItem
{

   public ClassAnnotatedItem(Class<?> clazz)
   {
      super(clazz, buildAnnotationMap(clazz));
      
   }
   
   protected static Map<Class<? extends Annotation>, Annotation> buildAnnotationMap(Class<?> clazz)
   {
      Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
      for (Annotation annotation : clazz.getAnnotations())
      {
         annotationMap.put(annotation.annotationType(), annotation);
      }
      return annotationMap;
   }
}
