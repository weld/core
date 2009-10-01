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
package org.jboss.webbeans.bean;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.interceptor.Interceptor;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bean.proxy.EnterpriseBeanInstance;
import org.jboss.webbeans.bean.proxy.EnterpriseBeanProxyMethodHandler;
import org.jboss.webbeans.bean.proxy.Marker;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.ejb.InternalEjbDescriptor;
import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.injection.InjectionContextImpl;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Proxies;

/**
 * An enterprise bean representation
 * 
 * @author Pete Muir
 * 
 * @param <T> The type (class) of the bean
 */
public class SessionBean<T> extends AbstractClassBean<T>
{
   private final Log log = Logging.getLog(SessionBean.class);

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
      WBClass<T> type = manager.getServices().get(ClassTransformer.class).loadClass(ejbDescriptor.getBeanClass());
      return new SessionBean<T>(type, ejbDescriptor, new StringBuilder().append(SessionBean.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(ejbDescriptor.getEjbName()).toString(), manager);
   }

   /**
    * Constructor
    * 
    * @param type The type of the bean
    * @param manager The Web Beans manager
    */
   protected SessionBean(WBClass<T> type, InternalEjbDescriptor<T> ejbDescriptor, String idSuffix, BeanManagerImpl manager)
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
      }
   }

   @Override
   protected void initTypes()
   {
      Set<Type> types = new LinkedHashSet<Type>();
      types.add(Object.class);
      for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces())
      {
         types.add(businessInterfaceDescriptor.getInterface());
      }
      super.types = types;
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
         throw new DefinitionException("Scope " + getScope() + " is not allowed on stateless enterpise beans for " + getType() + ". Only @Dependent is allowed on stateless enterprise beans");
      }
      if (ejbDescriptor.isSingleton() && !(isDependent() || getScope().equals(ApplicationScoped.class)))
      {
         throw new DefinitionException("Scope " + getScope() + " is not allowed on singleton enterpise beans for " + getType() + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise beans");
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
      if (!environment.getEjbDescriptors().contains(getAnnotatedItem().getWBSuperclass().getJavaClass()))
      {
         throw new DefinitionException("Annotation defined specializing EJB must have EJB superclass");
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      if (environment.getClassBean(getAnnotatedItem().getWBSuperclass()) == null)
      {
         throw new IllegalStateException(toString() + " does not specialize a bean");
      }
      AbstractClassBean<?> specializedBean = environment.getClassBean(getAnnotatedItem().getWBSuperclass());
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
      T instance = produce(creationalContext);
      if (hasDecorators())
      {
         instance = applyDecorators(instance, creationalContext, null);
      }
      return instance;
   }
   
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

   public T produce(CreationalContext<T> ctx)
   {
      try
      {
         T instance = proxyClass.newInstance();
         ctx.push(instance);
         ((ProxyObject) instance).setHandler(new EnterpriseBeanProxyMethodHandler<T>(this, ctx));
         log.trace("Enterprise bean instance created for bean {0}", this);
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
      EnterpriseBeanInstance enterpiseBeanInstance = (EnterpriseBeanInstance) instance;
      enterpiseBeanInstance.destroy(Marker.INSTANCE, this, creationalContext);
      creationalContext.release();
   }

   /**
    * Validates the bean type
    */
   private void checkEJBTypeAllowed()
   {
      if (ejbDescriptor.isMessageDriven())
      {
         throw new DefinitionException("Message Driven Beans can't be Web Beans");
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

   public void preDestroy(CreationalContext<T> creationalContext)
   {
      creationalContext.release();
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
      for (WBMethod<?, ?> method : this.annotatedItem.getWBDeclaredMethodsWithAnnotatedParameters(Observes.class))
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
   public boolean isMethodExistsOnTypes(WBMethod<?, ?> method)
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
   
}

