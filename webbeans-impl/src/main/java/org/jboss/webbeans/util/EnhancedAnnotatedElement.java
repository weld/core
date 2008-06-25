package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface EnhancedAnnotatedElement
{
   
   public abstract <T extends Annotation> Set<T> getAnnotations();
   
   public abstract <T extends Annotation> Set<Annotation> getAnnotations(
         Class<T> metaAnnotationType);
   
   public <T extends Annotation> T getAnnotation(Class<T> arg0);
   
   public boolean isAnnotationPresent(Class<? extends Annotation> arg0);
   
}