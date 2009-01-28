package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.context.CreationalContext;
import javax.inject.Produces;
import javax.inject.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.ForwardingAnnotatedConstructor;

public class ConstructorInjectionPoint<T> extends ForwardingAnnotatedConstructor<T> implements AnnotatedInjectionPoint<T, Constructor<T>>
{
   
   private abstract class ForwardingParameterInjectionPointList extends AbstractList<ParameterInjectionPoint<?>>
   {
      
      protected abstract List<? extends AnnotatedParameter<?>> delegate();
      
      protected abstract Bean<?> declaringBean();;

      @Override
      public ParameterInjectionPoint<?> get(int index)
      {
         return ParameterInjectionPoint.of(declaringBean, delegate().get(index));
      }
      
      @Override
      public int size()
      {
         return delegate().size();
      }
      
   }
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   
   private final Bean<?> declaringBean;
   private final AnnotatedConstructor<T> constructor;

   public static <T> ConstructorInjectionPoint<T> of(Bean<?> declaringBean, AnnotatedConstructor<T> constructor)
   {
      return new ConstructorInjectionPoint<T>(declaringBean, constructor);
   }
   
   protected ConstructorInjectionPoint(Bean<?> declaringBean, AnnotatedConstructor<T> constructor)
   {
      this.declaringBean = declaringBean;
      this.constructor = constructor;
   }
   
   @Override
   protected AnnotatedConstructor<T> delegate()
   {
      return constructor;
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
   
   public T newInstance(ManagerImpl manager, CreationalContext<?> creationalContext)
   {
      return delegate().newInstance(getParameterValues(getParameters(), null, null, manager, creationalContext));
   }
   
   @Override
   public List<ParameterInjectionPoint<?>> getParameters()
   {
      final List<? extends AnnotatedParameter<?>> delegate = super.getParameters();
      return new ForwardingParameterInjectionPointList()
      {

         @Override
         protected Bean<?> declaringBean()
         {
            return declaringBean;
         }

         @Override
         protected List<? extends AnnotatedParameter<?>> delegate()
         {
            return delegate;
         }
         
      };
   }
   
   public void inject(Object declaringInstance, Object value)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Helper method for getting the current parameter values from a list of
    * annotated parameters.
    * 
    * @param parameters The list of annotated parameter to look up
    * @param manager The Web Beans manager
    * @return The object array of looked up values
    */
   protected Object[] getParameterValues(List<ParameterInjectionPoint<?>> parameters, Object specialVal, Class<? extends Annotation> specialParam, ManagerImpl manager, CreationalContext<?> creationalContext)
   {
      Object[] parameterValues = new Object[parameters.size()];
      boolean producerMethod = this.isAnnotationPresent(Produces.class);
      InjectionPointProvider injectionPointProvider = manager.getInjectionPointProvider();
      Iterator<ParameterInjectionPoint<?>> iterator = parameters.iterator();
      for (int i = 0; i < parameterValues.length; i++)
      {
         ParameterInjectionPoint<?> param = iterator.next();
         if (specialParam != null && param.isAnnotationPresent(specialParam))
         {
            parameterValues[i] = specialVal;
         }
         else
         {
            if (!producerMethod)
            {
               injectionPointProvider.pushInjectionPoint(param);
            }
            try
            {
               parameterValues[i] = param.getValueToInject(manager, creationalContext);
            }
            finally
            {
               if (!producerMethod)
               {
                  injectionPointProvider.popInjectionPoint();
               }
            }
         }
      }
      return parameterValues;
   }

}
