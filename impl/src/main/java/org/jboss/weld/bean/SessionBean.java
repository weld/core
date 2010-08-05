/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.messages.BeanMessage.CANNOT_DESTROY_ENTERPRISE_BEAN_NOT_CREATED;
import static org.jboss.weld.logging.messages.BeanMessage.CANNOT_DESTROY_NULL_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.EJB_CANNOT_BE_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.EJB_CANNOT_BE_INTERCEPTOR;
import static org.jboss.weld.logging.messages.BeanMessage.EJB_NOT_FOUND;
import static org.jboss.weld.logging.messages.BeanMessage.GENERIC_SESSION_BEAN_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.MESSAGE_DRIVEN_BEANS_CANNOT_BE_MANAGED;
import static org.jboss.weld.logging.messages.BeanMessage.OBSERVER_METHOD_MUST_BE_STATIC_OR_BUSINESS;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.SCOPE_NOT_ALLOWED_ON_SINGLETON_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.SCOPE_NOT_ALLOWED_ON_STATELESS_SESSION_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.interceptor.Interceptor;

import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.bean.proxy.EnterpriseBeanInstance;
import org.jboss.weld.bean.proxy.EnterpriseBeanProxyMethodHandler;
import org.jboss.weld.bean.proxy.EnterpriseProxyFactory;
import org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance;
import org.jboss.weld.bean.proxy.Marker;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.exceptions.CreationException;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.SecureReflections;

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
    * @param beanManager the current manager
    * @return An Enterprise Web Bean
    */
   public static <T> SessionBean<T> of(InternalEjbDescriptor<T> ejbDescriptor, BeanManagerImpl beanManager)
   {
      WeldClass<T> type = beanManager.getServices().get(ClassTransformer.class).loadClass(ejbDescriptor.getBeanClass());
      return new SessionBean<T>(type, ejbDescriptor, createId(SessionBean.class.getSimpleName(), ejbDescriptor, type), beanManager);
   }

   /**
    * Creates a simple, annotation defined Enterprise Web Bean using the annotations specified on type
    * 
    * @param <T> The type
    * @param clazz The class
    * @param beanManager the current manager
    * @param type the AnnotatedType to use
    * @return An Enterprise Web Bean
    */
   public static <T> SessionBean<T> of(InternalEjbDescriptor<T> ejbDescriptor, BeanManagerImpl beanManager, WeldClass<T> type)
   {
      return new SessionBean<T>(type, ejbDescriptor, createId(SessionBean.class.getSimpleName(), ejbDescriptor, type), beanManager);
   }

   protected static String createId(String beanType, InternalEjbDescriptor<?> ejbDescriptor)
   {
      return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(ejbDescriptor.getEjbName()).toString();
   }

   protected static String createId(String beanType, InternalEjbDescriptor<?> ejbDescriptor, WeldClass<?> type)
   {
      if (type.isDiscovered())
      {
         return createId(beanType, ejbDescriptor);
      }
      else
      {
         return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(ejbDescriptor.getEjbName()).append(AnnotatedTypes.createTypeId(type)).toString();
      }
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
      initQualifiers();
      initConstructor();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         checkConstructor();
         super.initialize(environment);
         initProxyClass();
         checkEJBTypeAllowed();
         checkConflictingRoles();
         //checkObserverMethods();
         checkScopeAllowed();
         registerInterceptors();
         setInjectionTarget(new InjectionTarget<T>()
         {

            public void inject(final T instance, final CreationalContext<T> ctx)
            {
               new InjectionContextImpl<T>(getBeanManager(), this, instance)
               {
                  
                  public void proceed()
                  {
                     Beans.injectFieldsAndInitializers(instance, ctx, getBeanManager(), getInjectableFields(), getInitializerMethods());
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
               return (Set) getWeldInjectionPoints();
            }

            public T produce(CreationalContext<T> ctx)
            {
               return SessionBean.this.createInstance(ctx);
            }
            
         });
      }
   }
   
   protected T createInstance(CreationalContext<T> ctx) 
   {
      return getConstructor().newInstance(beanManager, ctx);
   }

   @Override
   protected void initTypes()
   {
      Map<Class<?>, Type> types = new LinkedHashMap<Class<?>, Type>();
      
      for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces())
      {
         types.putAll(new HierarchyDiscovery(businessInterfaceDescriptor.getInterface()).getTypeMap());
      }
      if (getWeldAnnotated().isAnnotationPresent(Typed.class))
      {
         super.types = getTypedTypes(types, getWeldAnnotated().getJavaClass(), getWeldAnnotated().getAnnotation(Typed.class));
      }
      else
      {
         types.put(Object.class, Object.class);
         super.types = new HashSet<Type>(types.values());
      }
   }

   protected void initProxyClass()
   {
      this.proxyClass = new EnterpriseProxyFactory<T>(getWeldAnnotated().getJavaClass(), getTypes()).getProxyClass();
   }

   /**
    * Validates for non-conflicting roles
    */
   protected void checkConflictingRoles()
   {
      if (getType().isAnnotationPresent(Interceptor.class))
      {
         throw new DefinitionException(EJB_CANNOT_BE_INTERCEPTOR, getType());
      }
      if (getType().isAnnotationPresent(Decorator.class))
      {
         throw new DefinitionException(EJB_CANNOT_BE_DECORATOR, getType());
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
         throw new DefinitionException(SCOPE_NOT_ALLOWED_ON_STATELESS_SESSION_BEAN, getScope(), getType());
      }
      if (ejbDescriptor.isSingleton() && !(isDependent() || getScope().equals(ApplicationScoped.class)))
      {
         throw new DefinitionException(SCOPE_NOT_ALLOWED_ON_SINGLETON_BEAN, getScope(), getType());
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
      if (!environment.getEjbDescriptors().contains(getWeldAnnotated().getWeldSuperclass().getJavaClass()))
      {
         throw new DefinitionException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      if (environment.getClassBean(getWeldAnnotated().getWeldSuperclass()) == null)
      {
         throw new IllegalStateException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
      }
      AbstractClassBean<?> specializedBean = environment.getClassBean(getWeldAnnotated().getWeldSuperclass());
      if (!(specializedBean instanceof SessionBean<?>))
      {
         throw new IllegalStateException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
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
      try
      {
         T instance = SecureReflections.newInstance(proxyClass);
         creationalContext.push(instance);
         ProxyFactory.setBeanInstance(instance, new EnterpriseTargetBeanInstance(getWeldAnnotated().getJavaClass(), new EnterpriseBeanProxyMethodHandler<T>(SessionBean.this, creationalContext)));
         if (hasDecorators())
         {
            instance = applyDecorators(instance, creationalContext, null);
         }
         return instance;
      }
      catch (InstantiationException e)
      {
         throw new WeldException(PROXY_INSTANTIATION_FAILED, e, this);
      }
      catch (IllegalAccessException e)
      {
         throw new WeldException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
      }
      catch (Exception e)
      {
         throw new CreationException(EJB_NOT_FOUND, e, proxyClass);
      }
      
   }

   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      if (instance == null)
      {
         throw new IllegalArgumentException(CANNOT_DESTROY_NULL_BEAN, this);
      }
      if (!(instance instanceof EnterpriseBeanInstance))
      {
         throw new IllegalArgumentException(CANNOT_DESTROY_ENTERPRISE_BEAN_NOT_CREATED, instance);
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
         throw new DefinitionException(MESSAGE_DRIVEN_BEANS_CANNOT_BE_MANAGED, this);
      }
   }
   
   @Override
   protected void checkType()
   {
      if (!getScope().equals(Dependent.class) && getWeldAnnotated().isGeneric())
      {
         throw new DefinitionException(GENERIC_SESSION_BEAN_MUST_BE_DEPENDENT, this);
      }
   }
   
   @Override
   public boolean isPassivationCapableBean()
   {
      return getEjbDescriptor().isStateful();
   }
   
   @Override
   public boolean isPassivationCapableDependency()
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
               throw new DefinitionException(OBSERVER_METHOD_MUST_BE_STATIC_OR_BUSINESS, method, getWeldAnnotated());
            }
         }
      }
   }
   
   // TODO must be a nicer way to do this!
   private boolean isMethodExistsOnTypes(WeldMethod<?, ?> method)
   {
      for (Type type : getTypes())
      {
         if (type instanceof Class<?>)
         {
            for (Method m : SecureReflections.getMethods((Class<?>) type))
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
      return beanManager.getServices().get(EjbServices.class).resolveEjb(getEjbDescriptor().delegate());
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
      InterceptionModel<Class<?>, SerializableContextual<javax.enterprise.inject.spi.Interceptor<?>, ?>> model = beanManager.getCdiInterceptorsRegistry().getInterceptionModel(ejbDescriptor.getBeanClass());
      if (model != null)
      {
         getBeanManager().getServices().get(EjbServices.class).registerInterceptors(getEjbDescriptor().delegate(), new InterceptorBindingsAdapter(model));
      }
   }
   
   @Override
   public String toString()
   {
      return "Session bean [" + getBeanClass() + " with qualifiers [" + Formats.formatAnnotations(getQualifiers()) + "]; local interfaces are [" + Formats.formatBusinessInterfaceDescriptors(getEjbDescriptor().getLocalBusinessInterfaces()) +"]";
   }
}

