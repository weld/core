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
package org.jboss.webbeans.bean.builtin;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.security.spi.SecurityServices;
import org.jboss.webbeans.util.collections.Arrays2;

/**
 * @author pmuir
 *
 */
public class PrincipalBean extends AbstractBuiltInBean<Principal>
{

   private static final Set<Type> TYPES = Arrays2.<Type>asSet(Object.class, Principal.class);
   
   public PrincipalBean(BeanManagerImpl manager)
   {
      super(manager);
   }

   @Override
   public Class<Principal> getType()
   {
      return Principal.class;
   }

   public Set<Type> getTypes()
   {
      return TYPES;
   }

   public Principal create(CreationalContext<Principal> creationalContext)
   {
      if (getManager().getServices().contains(SecurityServices.class))
      {
         return getManager().getServices().get(SecurityServices.class).getPrincipal();
      }
      else
      {
         throw new IllegalStateException("SecurityServices not available");
      }
   }

   public void destroy(Principal instance, CreationalContext<Principal> creationalContext)
   {
      // No-op      
   }

}
