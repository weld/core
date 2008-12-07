package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;

public class ApiAbstraction
{
   
   public @interface DummyAnnotation
   {
   }
   

   public interface Dummy 
   {
   }
   
   /**
    * Initializes an annotation class
    * 
    * @param name The name of the annotation class
    * @return The instance of the annotation. Returns a dummy if the class was
    *         not found
    */
   @SuppressWarnings("unchecked")
   protected static Class<? extends Annotation> annotationTypeForName(String name)
   {
      try
      {
         return (Class<? extends Annotation>) Reflections.classForName(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         return DummyAnnotation.class;
      }
   }
   
   @SuppressWarnings("unchecked")
   protected static Class<?> classForName(String name)
   {
      try
      {
         return (Class<? extends Annotation>) Reflections.classForName(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         return Dummy.class;
      }
   }
   
   
   
}
