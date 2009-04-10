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
package org.jboss.webbeans.bean.ee;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.context.CreationalContext;
import javax.context.Dependent;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;
import org.jboss.webbeans.util.Proxies;

/**
 * Representation of a Java EE Resource bean
 * 
 * @author Pete Muir
 *
 */
public abstract class AbstractJavaEEResourceBean<T> extends RIBean<T>
{
   
   private final Class<? extends Annotation> deploymentType;
   private final Set<Annotation> bindings;
   private final Class<T> type;
   private final Set<Type> types;
   private final Class<T> proxyClass;
   
   /**
    * @param manager the manager used to create this bean
    * @param deploymentType the deployment type of the bean
    * @param bindings the bindings of bean
    * @param type the concrete type of the bean
    */
   protected AbstractJavaEEResourceBean(ManagerImpl manager, Class<? extends Annotation> deploymentType, Set<Annotation> bindings, Class<T> type)
   {
      this(manager, deploymentType, bindings, type, type);
   }
   
   /**
    * @param manager the manager used to create this bean
    * @param deploymentType the deployment type of the bean
    * @param bindings the bindings of bean
    * @param type the concrete type of the bean
    */
   protected AbstractJavaEEResourceBean(ManagerImpl manager, Class<? extends Annotation> deploymentType, Set<Annotation> bindings, Class<T> type, Type... types)
   {
      super(manager);
      this.deploymentType = deploymentType;
      this.bindings = bindings;
      this.type = type;
      this.types = new HashSet<Type>();
      this.types.addAll(Arrays.asList(types));
      ProxyFactory proxyFactory = Proxies.getProxyFactory(this.types);

      @SuppressWarnings("unchecked")
      Class<T> proxyClass = proxyFactory.createClass();
      
      this.proxyClass = proxyClass;
   }

   @Override
   public Set<Annotation> getBindings()
   {
      return bindings;
   }
   
   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return Dependent.class;
   }
   
   @Override
   public String getName()
   {
      return null;
   }
   
   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return deploymentType;
   }
   
   @Override
   public Class<T> getType()
   {
      return type;
   }
   
   @Override
   public Set<? extends Type> getTypes()
   {
      return Collections.unmodifiableSet(types);
   }
   
   @Override
   public boolean isSpecializing()
   {
      return false;
   }
   
   @Override
   public RIBean<?> getSpecializedBean()
   {
      return null;
   }
   
   @Override
   public boolean isDependent()
   {
      return true;
   }
   
   @Override
   public Set<AnnotatedInjectionPoint<?, ?>> getInjectionPoints()
   {
      return Collections.emptySet();
   }
   
   @Override
   public boolean isNullable()
   {
      return true;
   }
   
   @Override
   public boolean isPrimitive()
   {
      return false;
   }
   
   @Override
   public boolean isSerializable()
   {
      return true;
   }

   @Override
   public boolean isProxyable()
   {
      return false;
   }
   
   protected Class<T> getProxyClass()
   {
      return proxyClass;
   }
   
   public T create(CreationalContext<T> creationalContext)
   {
      T instance;
      try
      {
         instance = getProxyClass().newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException("Error creating proxy for " + this, e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("Error creating proxy for " + this, e);
      }
      ((ProxyObject) instance).setHandler(newMethodHandler());
      return instance; 
   }
   
   protected abstract MethodHandler newMethodHandler();
   
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      // TODO Auto-generated method stub
      
   }
   
   public void destroy(T instance) 
   {
      
   }
   
}
