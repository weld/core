package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public class WrappedAnnotatedParameter<T> extends ForwardingAnnotatedParameter<T>
{
   
   public static <T> WrappedAnnotatedParameter<T> of(AnnotatedParameter<T> parameter, Set<Annotation> extraAnnotations)
   {
      return new WrappedAnnotatedParameter<T>(parameter, extraAnnotations);
   }
   
   private final AnnotatedParameter<T> delegate;
   private AnnotationStore annotationStore;
   
   public WrappedAnnotatedParameter(AnnotatedParameter<T> parameter, Set<Annotation> extraAnnotations)
   {
      this.delegate = parameter;
      this.annotationStore = AnnotationStore.wrap(parameter.getAnnotationStore(), extraAnnotations, extraAnnotations);
   }
   
   @Override
   protected AnnotatedParameter<T> delegate()
   {
      return delegate;
   }
   
   @Override
   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }
   
}
