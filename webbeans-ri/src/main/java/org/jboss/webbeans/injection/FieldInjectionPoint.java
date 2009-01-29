package org.jboss.webbeans.injection;

import static org.jboss.webbeans.injection.Exceptions.rethrowException;

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
      try
      {
         delegate().set(declaringInstance, manager.getInstanceToInject(this, creationalContext));
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e);
      }
   }
   
   public void inject(Object declaringInstance, Object value)
   {
      try
      {
         delegate().set(declaringInstance, value);
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e);
      }
   }

}
