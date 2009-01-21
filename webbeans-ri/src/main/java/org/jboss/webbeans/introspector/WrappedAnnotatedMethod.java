package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public class WrappedAnnotatedMethod<T> extends ForwardingAnnotatedMethod<T>
{
   
   private final AnnotatedMethod<T> delegate;
   private AnnotationStore annotationStore;
   
   public WrappedAnnotatedMethod(AnnotatedMethod<T> method, Set<Annotation> extraAnnotations)
   {
      this.delegate = method;
      this.annotationStore = AnnotationStore.wrap(method.getAnnotationStore(), extraAnnotations, extraAnnotations);
   }
   
   @Override
   protected AnnotatedMethod<T> delegate()
   {
      return delegate;
   }
   
   @Override
   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }
   
}
