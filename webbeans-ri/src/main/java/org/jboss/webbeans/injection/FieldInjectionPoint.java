package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import javax.context.CreationalContext;
import javax.inject.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.ForwardingAnnotatedField;

public class FieldInjectionPoint<T> extends ForwardingAnnotatedField<T> implements AnnotatedInjectionPoint<T, Field>
{
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   
   private final Bean<?> declaringBean;
   private final AnnotatedField<T> field;

   public static <T> FieldInjectionPoint<T> of(Bean<?> declaringBean, AnnotatedField<T> field)
   {
      return new FieldInjectionPoint<T>(declaringBean, field);
   }
   
   protected FieldInjectionPoint(Bean<?> declaringBean, AnnotatedField<T> field)
   {
      this.declaringBean = declaringBean;
      this.field = field;
   }

   @Override
   protected AnnotatedField<T> delegate()
   {
      return field;
   }

   public Annotation[] getAnnotations()
   {
      return delegate().getAnnotationStore().getAnnotations().toArray(EMPTY_ANNOTATION_ARRAY);
   }

   public Bean<?> getBean()
   {
      return declaringBean;
   }

   public Set<Annotation> getBindings()
   {
      return delegate().getAnnotationStore().getBindingTypes();
   }

   public void inject(Object declaringInstance, ManagerImpl manager, CreationalContext<?> creationalContext)
   {
      delegate().inject(declaringInstance, manager.getInstanceToInject(this, creationalContext));
   }

}
