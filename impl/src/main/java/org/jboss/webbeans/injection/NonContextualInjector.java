package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.context.CreationalContext;
import javax.context.Dependent;
import javax.inject.Standard;
import javax.inject.manager.Bean;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.collections.ConcurrentCache;

public class NonContextualInjector
{
   
   private final Bean<?> nonContextualBean;
   
   private final ConcurrentCache<Class<?>, Set<FieldInjectionPoint<?>>> instances;
   private final ManagerImpl manager;

   public NonContextualInjector(ManagerImpl manager)
   {
      this.instances = new ConcurrentCache<Class<?>, Set<FieldInjectionPoint<?>>>();
      this.manager = manager;
      nonContextualBean = new Bean<Object>(manager)
      {
         
         @Override
         public Set<Annotation> getBindings()
         {
            return Collections.emptySet();
         }

         @Override
         public Class<? extends Annotation> getDeploymentType()
         {
            return Standard.class;
         }

         @Override
         public Set<? extends InjectionPoint> getInjectionPoints()
         {
            return Collections.emptySet();
         }

         @Override
         public String getName()
         {
            return null;
         }

         @Override
         public Class<? extends Annotation> getScopeType()
         {
            return Dependent.class;
         }

         @Override
         public Set<? extends Type> getTypes()
         {
            return Collections.emptySet();
         }

         @Override
         public boolean isNullable()
         {
            return false;
         }

         @Override
         public boolean isSerializable()
         {
            return true;
         }

         public Object create(CreationalContext<Object> creationalContext)
         {
            return null;
         }

         public void destroy(Object instance)
         {
         }
      };
   }   
   
   public void inject(final Object instance)
   {
      if (DependentContext.instance() != null && ApplicationContext.instance() != null)
      {
         DependentContext.instance().setActive(true);
         ApplicationContext.instance().setActive(true);
         Set<FieldInjectionPoint<?>> injectionPoints = instances.putIfAbsent(instance.getClass(), new Callable<Set<FieldInjectionPoint<?>>>()
         {
            
            public Set<FieldInjectionPoint<?>> call() throws Exception
            {
               return Beans.getFieldInjectionPoints(manager.getServices().get(ClassTransformer.class).classForName(instance.getClass()), nonContextualBean);
            }
            
         }
         );
         for (FieldInjectionPoint<?> injectionPoint : injectionPoints)
         {
            injectionPoint.inject(instance, manager, null);
         }
         DependentContext.instance().setActive(false);
         ApplicationContext.instance().setActive(false);
      }
   }
   
}
