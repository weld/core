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

import java.lang.reflect.Type;
import java.util.HashSet;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.CreationException;
import javax.webbeans.Decorator;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Interceptor;
import javax.webbeans.Specializes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.proxy.EnterpriseBeanProxyMethodHandler;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.ejb.InternalEjbDescriptor;
import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Proxies;
import org.jboss.webbeans.util.Reflections;

/**
 * An enterprise bean representation
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class EnterpriseBean<T> extends AbstractClassBean<T>
{
   private LogProvider log = Logging.getLogProvider(EnterpriseBean.class);

   // The EJB descriptor
   private InternalEjbDescriptor<T> ejbDescriptor;

   private Class<T> proxyClass;
   
   private EnterpriseBean<?> specializedBean;

   /**
    * Creates a simple, annotation defined Enterprise Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return An Enterprise Web Bean
    */
   public static <T> EnterpriseBean<T> of(Class<T> clazz, ManagerImpl manager)
   {
      return of(AnnotatedClassImpl.of(clazz), manager);
   }

   public static <T> EnterpriseBean<T> of(AnnotatedClass<T> clazz, ManagerImpl manager)
   {
      return new EnterpriseBean<T>(clazz, manager);
   }

   /**
    * Constructor
    * 
    * @param type The type of the bean
    * @param manager The Web Beans manager
    */
   protected EnterpriseBean(AnnotatedClass<T> type, ManagerImpl manager)
   {
      super(type, manager);
      if (type.isAnnotationPresent(Specializes.class))
      {
         this.specializedBean = EnterpriseBean.of(type.getSuperclass(), manager);
      }
      init();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      initType();
      Iterable<InternalEjbDescriptor<T>> ejbDescriptors = manager.getEjbDescriptorCache().get(getType());
      if (ejbDescriptors == null)
      {
         throw new DefinitionException("Not an EJB " + toString());
      }
      for (InternalEjbDescriptor<T> ejbDescriptor : ejbDescriptors)
      {
         if (this.ejbDescriptor == null)
         {
            this.ejbDescriptor = ejbDescriptor;
         }
         else
         {
            throw new RuntimeException("TODO Multiple EJBs have the same bean class! " + getType());
         }
      }
      super.init();
      initProxyClass();
      initInjectionPoints();
      checkEnterpriseBeanTypeAllowed();
      checkEnterpriseScopeAllowed();
      checkConflictingRoles();
   }

   /**
    * Initializes the injection points
    */
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
   }

   protected void initTypes()
   {
      types = new HashSet<Type>();
      for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces())
      {
         types.add(businessInterfaceDescriptor.getInterface());
      }
      types.add(Object.class);
   }

   protected void initProxyClass()
   {
      ProxyFactory proxyFactory = Proxies.getProxyFactory(getTypes());

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
   protected void checkEnterpriseScopeAllowed()
   {
      if (ejbDescriptor.isStateless() && !getScopeType().equals(Dependent.class))
      {
         throw new DefinitionException("Scope " + getScopeType() + " is not allowed on stateless enterpise beans for " + getType() + ". Only @Dependent is allowed on stateless enterprise beans");
      }
      if (ejbDescriptor.isSingleton() && (!(getScopeType().equals(Dependent.class) || getScopeType().equals(ApplicationScoped.class))))
      {
         throw new DefinitionException("Scope " + getScopeType() + " is not allowed on singleton enterpise beans for " + getType() + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise beans");
      }
   }

   /**
    * Validates specialization
    */
   protected void preCheckSpecialization()
   {
      super.preCheckSpecialization();
      if (!manager.getEjbDescriptorCache().containsKey(getAnnotatedItem().getSuperclass().getType()))
      {
         throw new DefinitionException("Annotation defined specializing EJB must have EJB superclass");
      }
   }
   
   @Override
   protected void initSpecialization()
   {
      this.specializedBean = EnterpriseBean.of(getAnnotatedItem().getSuperclass(), manager);
   }

   /**
    * Creates an instance of the bean
    * 
    * @return The instance
    */
   @Override
   public T create()
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         T instance = proxyClass.newInstance();
         ((ProxyObject) instance).setHandler(new EnterpriseBeanProxyMethodHandler(this, ejbDescriptor.getRemoveMethods()));
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
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   /**
    * Destroys an instance of the bean
    * 
    * @param instance The instance
    */
   @Override
   public void destroy(T instance)
   {
      Boolean isDestroyed = (Boolean) Reflections.invokeAndWrap("isDestroyed", null, instance, null);
      if (isDestroyed.booleanValue())
      {
         return;
      }
   }

   /**
    * Calls all initializers of the bean
    * 
    * @param instance The bean instance
    */
   protected void callInitializers(T instance)
   {
      for (AnnotatedMethod<?> initializer : getInitializerMethods())
      {
         initializer.invoke(manager, instance);
      }
   }

   /**
    * Injects EJBs and common fields
    */
   protected void injectEjbAndCommonFields()
   {
      // TODO Support commons and EJB annotations
   }

   /**
    * Validates the bean type
    */
   private void checkEnterpriseBeanTypeAllowed()
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
   public String toString()
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
      buffer.append(" [" + getType().getName() + "]\n");
      buffer.append("   API types " + getTypes() + ", binding types " + getBindings() + "\n");
      return buffer.toString();
   }

   public void postConstruct(T instance)
   {
      try
      {
         manager.getInjectionPointProvider().pushBean(this);
         DependentContext.INSTANCE.setActive(true);
         bindDecorators();
         bindInterceptors();
         injectEjbAndCommonFields();
         injectBoundFields(instance);
         callInitializers(instance);
      }
      finally
      {
         manager.getInjectionPointProvider().popBean();
         DependentContext.INSTANCE.setActive(false);
      }

   }

   public void preDestroy(T instance)
   {
   }

   @Override
   public boolean isSerializable()
   {
      return injectionPointsAreSerializable();
   }

   public InternalEjbDescriptor<T> getEjbDescriptor()
   {
      return ejbDescriptor;
   }

   public boolean canCallRemoveMethods()
   {
      return getEjbDescriptor().isStateful() && Dependent.class.equals(getScopeType());
   }
   
   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return specializedBean;
   }

}
