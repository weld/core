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

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Abstract base class with functions specific to RI built-in beans
 *  
 * @author Pete Muir
 */
public abstract class RIBean<T> implements Bean<T>, PassivationCapable 
{
   
   public static final String BEAN_ID_PREFIX = RIBean.class.getPackage().getName();
   
   public static final String BEAN_ID_SEPARATOR = "-";
   
   private final BeanManagerImpl beanManager;
   
   private final String id;

   protected RIBean(String idSuffix, BeanManagerImpl beanManager)
   {
      this.beanManager = beanManager;
      this.id = new StringBuilder().append(BEAN_ID_PREFIX).append(BEAN_ID_SEPARATOR).append(beanManager.getId()).append(BEAN_ID_SEPARATOR).append(idSuffix).toString();
   }

   protected BeanManagerImpl getBeanManager()
   {
      return beanManager;
   }

   public abstract Class<T> getType();
   
   public Class<?> getBeanClass()
   {
      return getType();
   }
   
   public abstract void initialize(BeanDeployerEnvironment environment);

   /**
    * In particular cases, the deployer must perform some initialization operations
    * only after all beans have been deployed (e.g. for initializing decorators
    * taking into account the possibility of having custom decorators which are
    * deployed through portable extensions)
    * 
    * @param environment
    */
   public void initializeAfterBeanDiscovery()
   {
      // no-op by default
   }

    public void checkType()
    {

    }

   public abstract boolean isSpecializing();

   public boolean isDependent()
   {
      return getScope().equals(Dependent.class);
   }

   public abstract boolean isProxyable();
   
   public abstract boolean isPassivationCapableBean();
   
   public abstract boolean isPassivationCapableDependency();

   public boolean isProxyRequired()
   {
      return false;
   }
   
   public abstract boolean isPrimitive();

   public abstract Set<WeldInjectionPoint<?, ?>> getWeldInjectionPoints();
   
   public Set<InjectionPoint> getInjectionPoints()
   {
      return (Set) getWeldInjectionPoints();
   }

   public abstract RIBean<?> getSpecializedBean();
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof RIBean<?>)
      {
         RIBean<?> that = (RIBean<?>) obj;
         return this.getId().equals(that.getId());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getId().hashCode();
   }
   
   public String getId()
   {
      return id;
   }
   
   @Override
   public String toString()
   {
      return id;
   }

}
