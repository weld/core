/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bean;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.interceptor.Interceptor;

import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.bean.proxy.EnterpriseBeanInstance;
import org.jboss.weld.bean.proxy.EnterpriseBeanProxyMethodHandler;
import org.jboss.weld.bean.proxy.Marker;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Proxies;

/**
 * An enterprise bean representation
 * 
 * @author Pete Muir
 * 
 * @param <T> The type (class) of the bean
 */
public class SessionBean<T> extends AbstractClassBean<T>
{

   // The EJB descriptor
   private InternalEjbDescriptor<T> ejbDescriptor;

   private Class<T> proxyClass;

   private SessionBean<?> specializedBean;

   /**
    * Creates a simple, annotation defined Enterprise Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return An Enterprise Web Bean
    */
   public static <T> SessionBean<T> of(InternalEjbDescriptor<T> ejbDescriptor, BeanManagerImpl manager)
   {
      WeldClass<T> type = manager.getServices().get(ClassTransformer.class).loadClass(ejbDescriptor.getBeanClass());
      return new SessionBean<T>(type, ejbDescriptor, createId(SessionBean.class.getSimpleName(), ejbDescriptor) , manager);
   }

   protected static String createId(String beanType, InternalEjbDescriptor<?> ejbDescriptor)
   {
      return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(ejbDescriptor.getEjbName()).toString();
   }
   
   /**
    * Constructor
    * 
    * @param type The type of the bean
    * @param manager The Bean manager
    */
   protected SessionBean(WeldClass<T> type, InternalEjbDescriptor<T> ejbDescriptor, String idSuffix, BeanManagerImpl manager)
   {
      super(type, idSuffix, manager);
      initType();
      this.ejbDescriptor = ejbDescriptor;
      initTypes();
      initBindings();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         super.initialize(environment);
         initProxyClass();
         checkEJBTypeAllowed();
         checkConflictingRoles();
         checkObserverMethods();
         checkScopeAllowed();
         registerInterceptors();
         setInjectionTarget(new InjectionTarget<T>()
         {

            public void inject(final T instance, final CreationalContext<T> ctx)
            {
               new InjectionContextImpl<T>(getManager(), this, instance)
               {
                  
                  public void proceed()
                  {
                     Beans.injectFieldsAndInitializers(instance, ctx, getManager(), getInjectableFields(), getInitializerMethods());
                  }
                  
               }.run();
            }

            public void postConstruct(T instance)
            {
               defaultPostConstruct(instance);
            }

            public void preDestroy(T instance)
            {
               defaultPreDestroy(instance);
            }

            public void dispose(T instance)
            {
               // No-op
            }

            public Set<InjectionPoint> getInjectionPoints()
            {
               return (Set) getAnnotatedInjectionPoints();
            }

            public T produce(CreationalContext<T> ctx)
            {
               try
               {
                  T instance = proxyClass.newInstance();
                  ctx.push(instance);
                  ((ProxyObject) instance).setHandler(new EnterpriseBeanProxyMethodHandler<T>(SessionBean.this, ctx));
                  return instance;
               }
               catch (InstantiationException e)
               {
                  throw new RuntimeException("Could not instantiate enterprise proxy for " + toString(), e);
               }
               catch (IllegalAccessException e)
               {
                  throw new RuntimeException("Could not access bean correctly when creating enterprise proxy for " + toString(), e);
               }
               catch (Exception e)
               {
                  throw new CreationException("could not find the EJB in JNDI " + proxyClass, e);
               }
            }
            
         });
      }
   }

   @Override
   protected void initTypes()
   {
      Map<Class<?>, Type> types = new LinkedHashMap<Class<?>, Type>();
      for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces())
      {
         types.put(businessInterfaceDescriptor.getInterface(), businessInterfaceDescriptor.getInterface());
      }
      if (getAnnotatedItem().isAnnotationPresent(Typed.class))
      {
         super.types = getTypedTypes(types, getAnnotatedItem().getJavaClass(), getAnnotatedItem().getAnnotation(Typed.class));
      }
      else
      {
         types.put(Object.class, Object.class);
         super.types = new HashSet<Type>(types.values());
      }
   }

   protected void initProxyClass()
   {
      Set<Type> types = new LinkedHashSet<Type>(getTypes());
      types.add(EnterpriseBeanInstance.class);
      types.add(Serializable.class);
      ProxyFactory proxyFactory = Proxies.getProxyFactory(types);

      @SuppressWarnings("unchecked")
      Class<T> proxyClass = proxyFactory.createClass();

      this.proxyClass = proxyClass;
   }

   /**
    * Validates for non-conflicting roles
    */
   protected void checkConflictingRoles()
   {
      if (getType().isAnnotationPresent(Interceptor.class))
      {
         throw new DefinitionException("Enterprise beans cannot be interceptors");
      }
      if (getType().isAnnotationPresent(Decorator.class))
      {
         throw new DefinitionException("Enterprise beans cannot be decorators");
      }
   }

   /**
    * Check that the scope type is allowed by the stereotypes on the bean and
    * the bean type
    */
   protected void checkScopeAllowed()
   {
      if (ejbDescriptor.isStateless() && !isDependent())
      {
         throw new DefinitionException("Scope " + getScope() + " is not allowed on stateless enterprise beans for " + getType() + ". Only @Dependent is allowed on stateless enterprise beans");
      }
      if (ejbDescriptor.isSingleton() && !(isDependent() || getScope().equals(ApplicationScoped.class)))
      {
         throw new DefinitionException("Scope " + getScope() + " is not allowed on singleton enterprise beans for " + getType() + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise beans");
      }
   }

   /**
    * Validates specialization
    */
   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      super.preSpecialize(environment);
      // We appear to check this twice?
      if (!environment.getEjbDescriptors().contains(getAnnotatedItem().getWeldSuperclass().getJavaClass()))
      {
         throw new DefinitionException("Annotation defined specializing EJB must have EJB superclass");
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      if (environment.getClassBean(getAnnotatedItem().getWeldSuperclass()) == null)
      {
         throw new IllegalStateException(toString() + " does not specialize a bean");
      }
      AbstractClassBean<?> specializedBean = environment.getClassBean(getAnnotatedItem().getWeldSuperclass());
      if (!(specializedBean instanceof SessionBean<?>))
      {
         throw new IllegalStateException(toString() + " doesn't have a session bean as a superclass " + specializedBean);
      }
      else
      {
         this.specializedBean = (SessionBean<?>) specializedBean; 
      }
   }

   /**
    * Creates an instance of the bean
    * 
    * @return The instance
    */
   public T create(final CreationalContext<T> creationalContext)
   {
      T instance = getInjectionTarget().produce(creationalContext);
      if (hasDecorators())
      {
         instance = applyDecorators(instance, creationalContext, null);
      }
      return instance;
   }

   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      if (instance == null)
      {
         throw new IllegalArgumentException("instance to destroy cannot be null");
      }
      if (!(instance instanceof EnterpriseBeanInstance))
      {
         throw new IllegalArgumentException("Cannot destroy session bean instance not created by the container");
      }
      EnterpriseBeanInstance enterpriseBeanInstance = (EnterpriseBeanInstance) instance;
      enterpriseBeanInstance.destroy(Marker.INSTANCE, this, creationalContext);
      creationalContext.release();
   }

   /**
    * Validates the bean type
    */
   private void checkEJBTypeAllowed()
   {
      if (ejbDescriptor.isMessageDriven())
      {
         throw new DefinitionException("Message Driven Beans can't be Managed Beans");
      }
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String getDescription()
   {
      StringBuilder buffer = new StringBuilder();
      // buffer.append("Annotated " + Names.scopeTypeToString(getScopeType()) +
      // Names.ejbTypeFromMetaData(getEjbMetaData()));
      if (getName() == null)
      {
         buffer.append(" unnamed enterprise bean");
      }
      else
      {
         buffer.append(" enterprise bean '" + getName() + "'");
      }
      buffer.append(" [" + getType().getName() + "] ");
      buffer.append("API types " + getTypes() + ", binding types " + getQualifiers());
      return buffer.toString();
   }
   
   @Override
   protected void initSerializable()
   {
      // No-op
   }
   
   @Override
   public boolean isSerializable()
   {
      return true;
   }

   public InternalEjbDescriptor<T> getEjbDescriptor()
   {
      return ejbDescriptor;
   }

   public boolean isClientCanCallRemoveMethods()
   {
      return getEjbDescriptor().isStateful() && isDependent();
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return specializedBean;
   }

   /**
    * If there are any observer methods, they must be static or business
    * methods.
    */
   protected void checkObserverMethods()
   {
      for (WeldMethod<?, ?> method : this.annotatedItem.getDeclaredWeldMethodsWithAnnotatedParameters(Observes.class))
      {
         if (!method.isStatic())
         {
            if (!isMethodExistsOnTypes(method))
            {
               throw new DefinitionException("Observer method must be static or business method: " + method + " on " + getAnnotatedItem());
            }
         }
      }
   }
   
   // TODO must be a nicer way to do this!
   public boolean isMethodExistsOnTypes(WeldMethod<?, ?> method)
   {
      for (Type type : getTypes())
      {
         if (type instanceof Class)
         {
            for (Method m : ((Class<?>) type).getMethods())
            {
               if (method.getName().equals(m.getName()) && Arrays.equals(method.getParameterTypesAsArray(), m.getParameterTypes()))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }
   
   public SessionObjectReference createReference()
   {
      return manager.getServices().get(EjbServices.class).resolveEjb(getEjbDescriptor().delegate());
   }

   @Override
   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();
   }

   @Override
   protected boolean isInterceptionCandidate()
   {
      return true;
   }

   protected void registerInterceptors()
   {
      InterceptionModel<Class<?>, SerializableContextual<javax.enterprise.inject.spi.Interceptor<?>, ?>> model = manager.getCdiInterceptorsRegistry().getInterceptionModel(ejbDescriptor.getBeanClass());
      if (model != null)
         getManager().getServices().get(EjbServices.class).registerInterceptors(getEjbDescriptor().delegate(), new InterceptorBindingsAdapter(model));
   }
}

