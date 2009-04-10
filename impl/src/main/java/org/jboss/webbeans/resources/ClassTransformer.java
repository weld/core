package org.jboss.webbeans.resources;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedAnnotationImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.util.collections.ConcurrentCache;

public class ClassTransformer implements Service 
{
   
   private final ConcurrentCache<Class<?>, AnnotatedClass<?>> classes;
   private final ConcurrentCache<Class<?>, AnnotatedAnnotation<?>> annotations;
   private final ClassTransformer transformer = this;
   
   /**
    * 
    */
   public ClassTransformer()
   {
      classes = new ConcurrentCache<Class<?>, AnnotatedClass<?>>();
      annotations = new ConcurrentCache<Class<?>, AnnotatedAnnotation<?>>();
   }
   
   public <T> AnnotatedClass<T> classForName(final Class<T> clazz)
   {
      return classes.putIfAbsent(clazz, new Callable<AnnotatedClass<T>>()
      {

         public AnnotatedClass<T> call() throws Exception
         {
            return AnnotatedClassImpl.of(clazz, transformer);
         }
            
      });
   }
   
   public <T extends Annotation> AnnotatedAnnotation<T> classForName(final Class<T> clazz)
   {
      return annotations.putIfAbsent(clazz, new Callable<AnnotatedAnnotation<T>>()
      {

         public AnnotatedAnnotation<T> call() throws Exception
         {
            return AnnotatedAnnotationImpl.of(clazz, transformer);
         }
            
      });
   }

}
