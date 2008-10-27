package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;

import javax.webbeans.DefinitionException;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.SimpleAnnotatedAnnotation;

public abstract class AnnotationModel<T extends Annotation>
{
   
   private AnnotatedAnnotation<T> annotatedAnnotation;
   private boolean valid;
   
   public AnnotationModel(Class<T> type)
   {
      this.annotatedAnnotation = new SimpleAnnotatedAnnotation<T>(type);
      init();
   }
   
   protected void init()
   {
      initType();
      initValid();
   }
   
   protected void initType()
   {
      if (!Annotation.class.isAssignableFrom(getType()))
      {
         throw new DefinitionException(getMetaAnnotation().toString() + " can only be applied to an annotation, it was applied to " + getType());
      }
   }
   
   protected void initValid()
   {
      this.valid = annotatedAnnotation.isAnnotationPresent(getMetaAnnotation());
   }

   public Class<T> getType()
   {
      return annotatedAnnotation.getDelegate();
   }
   
   protected abstract Class<? extends Annotation> getMetaAnnotation();
   
   public boolean isValid()
   {
      return valid;
   }
   
   protected AnnotatedAnnotation<T> getAnnotatedAnnotation()
   {
      return annotatedAnnotation;
   }
   
}