package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public class WrappedAnnotatedClass<T> extends ForwardingAnnotatedClass<T>
{
   
   private final AnnotatedClass<T> delegate;
   private AnnotationStore annotationStore;
   
   public WrappedAnnotatedClass(AnnotatedClass<T> clazz, Set<Annotation> extraAnnotations, Set<Annotation> extraDeclaredAnnotations)
   {
      this.delegate = clazz;
      this.annotationStore = AnnotationStore.wrap(clazz.getAnnotationStore(), extraAnnotations, extraDeclaredAnnotations);
   }
   
   @Override
   protected AnnotatedClass<T> delegate()
   {
      return delegate;
   }
   
   @Override
   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }
   
}
