package org.jboss.webbeans.resources;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedAnnotationImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;

public class ClassTransformer implements Service 
{
   
   public <T> AnnotatedClass<T> classForName(Class<T> clazz)
   {
      return AnnotatedClassImpl.of(clazz, this);
   }
   
   public <T extends Annotation> AnnotatedAnnotation<T> classForName(Class<T> clazz)
   {
      return AnnotatedAnnotationImpl.of(clazz, this);
   }

}
