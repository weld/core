package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.inject.manager.Bean;
import javax.inject.manager.Manager;

import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.ForwardingAnnotatedMethod;

public class MethodInjectionPoint<T> extends ForwardingAnnotatedMethod<T> implements AnnotatedInjectionPoint<T, Method>
{
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   
   private final Bean<?> declaringBean;
   private final AnnotatedMethod<T> method;

   public static <T> MethodInjectionPoint<T> of(Bean<?> declaringBean, AnnotatedMethod<T> method)
   {
      return new MethodInjectionPoint<T>(declaringBean, method);
   }
   
   protected MethodInjectionPoint(Bean<?> declaringBean, AnnotatedMethod<T> method)
   {
      this.declaringBean = declaringBean;
      this.method = method;
   }
   
   @Override
   protected AnnotatedMethod<T> delegate()
   {
      return method;
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
      return delegate().getBindingTypes();
   }
   
   public void inject(Object declaringInstance, Manager manager)
   {
      delegate().invoke(declaringInstance, manager);
   }
   
   public void inject(Object declaringInstance, Object value)
   {
      delegate().invoke(declaringInstance, value);
   }
   
}
