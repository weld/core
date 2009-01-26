package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Set;

import javax.inject.manager.Bean;
import javax.inject.manager.Manager;

import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.ForwardingAnnotatedParameter;

public class ParameterInjectionPoint<T> extends ForwardingAnnotatedParameter<T> implements AnnotatedInjectionPoint<T, Object>
{
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   
   public static <T> ParameterInjectionPoint<T> of(Bean<?> declaringBean, AnnotatedParameter<T> parameter)
   {
      return new ParameterInjectionPoint<T>(declaringBean, parameter);
   }
   
   private final Bean<?> declaringBean;
   private final AnnotatedParameter<T> parameter;

   private ParameterInjectionPoint(Bean<?> declaringBean, AnnotatedParameter<T> parameter)
   {
      this.declaringBean = declaringBean;
      this.parameter = parameter;
   }

   @Override
   protected AnnotatedParameter<T> delegate()
   {
      return parameter;
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

   public Member getMember()
   {
      return delegate().getDeclaringMember().getMember();
   }
   
   public void inject(Object declaringInstance, Manager manager)
   {
      throw new UnsupportedOperationException();
   }
   
   public void inject(Object declaringInstance, Object value)
   {
      throw new UnsupportedOperationException();
   }

}
