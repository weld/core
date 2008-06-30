package org.jboss.webbeans.test.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Util
{

   public static boolean annotationSetMatches(Set<Annotation> annotations, Class<? extends Annotation>... annotationTypes)
   {
      List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
      annotationTypeList.addAll(Arrays.asList(annotationTypes));
      for (Annotation annotation : annotations)
      {
         if (annotationTypeList.contains(annotation.annotationType()))
         {
            annotationTypeList.remove(annotation.annotationType());
         }
         else
         {
            return false;
         }
      }
      return annotationTypeList.size() == 0;
   }
   
   public static boolean annotationTypeSetMatches(Set<Class<? extends Annotation>> annotations, Class<? extends Annotation>... annotationTypes)
   {
      List<Class<? extends Annotation>> annotationTypeList = new ArrayList<Class<? extends Annotation>>();
      annotationTypeList.addAll(Arrays.asList(annotationTypes));
      for (Class<? extends Annotation> annotation : annotations)
      {
         if (annotationTypeList.contains(annotation))
         {
            annotationTypeList.remove(annotation);
         }
         else
         {
            return false;
         }
      }
      return annotationTypeList.size() == 0;
   }
   
}
