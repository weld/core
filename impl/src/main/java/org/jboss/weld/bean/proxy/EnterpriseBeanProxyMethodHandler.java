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
package org.jboss.weld.bean.proxy;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CALL_PROXIED_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.CREATED_SESSION_BEAN_PROXY;
import static org.jboss.weld.logging.messages.BeanMessage.INVALID_REMOVE_METHOD_INVOCATION;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javassist.util.proxy.MethodHandler;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.InvalidOperationException;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.jlr.MethodSignatureImpl;
import org.jboss.weld.util.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * Method handler for enterprise bean client proxies
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 */
public class EnterpriseBeanProxyMethodHandler<T> implements MethodHandler, Serializable
{

   private static final long serialVersionUID = 2107723373882153667L;

   // The log provider
   private static final LocLogger log = loggerFactory().getLogger(BEAN);

   private final SessionObjectReference reference;
   private final Class<?> objectInterface;
   private final Collection<MethodSignature> removeMethodSignatures;
   private final boolean clientCanCallRemoveMethods;
   private final boolean stateful;

   /**
    * Constructor
    * 
    * @param removeMethods
    * 
    * @param proxy The generic proxy
    */
   public EnterpriseBeanProxyMethodHandler(SessionBean<T> bean, CreationalContext<T> creationalContext)
   {
      this.objectInterface = bean.getEjbDescriptor().getObjectInterface();
      this.removeMethodSignatures = bean.getEjbDescriptor().getRemoveMethodSignatures();
      this.clientCanCallRemoveMethods = bean.isClientCanCallRemoveMethods();
      this.reference = bean.createReference();
      this.stateful = bean.getEjbDescriptor().isStateful();
      log.trace(CREATED_SESSION_BEAN_PROXY, bean);
   }

   /**
    * Lookups the EJB in the container and executes the method on it
    * 
    * @param self the proxy instance.
    * @param method the overridden method declared in the super class or
    *           interface.
    * @param proceed the forwarder method for invoking the overridden method. It
    *           is null if the overridden method is abstract or declared in the
    *           interface.
    * @param args an array of objects containing the values of the arguments
    *           passed in the method invocation on the proxy instance. If a
    *           parameter type is a primitive type, the type of the array
    *           element is a wrapper class.
    * @return the resulting value of the method invocation.
    * 
    * @throws Throwable if the method invocation fails.
    */
   public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
   {
      if (reference.isRemoved())
      {
         return null;
      }
      if ("destroy".equals(method.getName()) && Marker.isMarker(0, method, args))
      {
         if (stateful)
         {
            reference.remove();
         }
         return null;
      }

      if (!clientCanCallRemoveMethods)
      {
         // TODO we can certainly optimize this search algorithm!
         MethodSignature methodSignature = new MethodSignatureImpl(method);
         if (removeMethodSignatures.contains(methodSignature))
         {
            throw new InvalidOperationException(INVALID_REMOVE_METHOD_INVOCATION, method);
         }
      }
      Class<?> businessInterface = getBusinessInterface(method);
      Object proxiedInstance = reference.getBusinessObject(businessInterface);
      Method proxiedMethod = Reflections.lookupMethod(method, proxiedInstance);
      try
      {
         Object returnValue = Reflections.invoke(proxiedMethod, proxiedInstance, args);
         log.trace(CALL_PROXIED_METHOD, method, proxiedInstance, args, returnValue);
         return returnValue;
      }
      catch (InvocationTargetException e)
      {
         if (e.getCause() != null)
         {
            throw e.getCause();
         }
         else
         {
            throw e;
         }
      }
   }

   private Class<?> getBusinessInterface(Method method)
   {
      Class<?> businessInterface = method.getDeclaringClass();
      if (businessInterface.equals(Object.class))
      {
         return objectInterface;
      }
      else
      {
         return businessInterface;
      }
   }

}
