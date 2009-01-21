package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public class WrappedAnnotatedField<T> extends ForwardingAnnotatedField<T>
{
   
   private final AnnotatedField<T> delegate;
   private AnnotationStore annotationStore;
   
   public WrappedAnnotatedField(AnnotatedField<T> field, Set<Annotation> extraAnnotations)
   {
      this.delegate = field;
      this.annotationStore = AnnotationStore.wrap(field.getAnnotationStore(), extraAnnotations, extraAnnotations);
   }
   
   @Override
   protected AnnotatedField<T> delegate()
   {
      return delegate;
   }
   
   @Override
   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }
   
}
