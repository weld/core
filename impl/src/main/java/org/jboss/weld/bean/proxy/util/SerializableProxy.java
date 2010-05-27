/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy.util;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_DESERIALIZATION_FAILURE;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.util.collections.ArraySet;

/**
 * A wrapper mostly for client proxies which provides header information useful
 * to generate the client proxy class in a VM before the proxy object is
 * deserialized. Only client proxies really need this extra step for
 * serialization and deserialization since the other proxy classes are generated
 * during bean archive deployment.
 * 
 * @author David Allen
 */
public class SerializableProxy implements Serializable
{

   private static final long serialVersionUID = -7682006876407447753L;

   // Information required to generate client proxy classes
   private final String      proxyClassName;
   private final String      proxySuperClassName;
   private final ArraySet<String> proxyInterfaces;

   // The wrapped proxy object not serialized by default actions
   private transient Object  proxyObject;
   private transient boolean writeProxy;

   public SerializableProxy(Object proxyObject)
   {
      if (!ProxyFactory.isProxy(proxyObject))
      {
         throw new IllegalStateException(PROXY_REQUIRED);
      }
      this.proxyClassName = proxyObject.getClass().getName();
      this.proxySuperClassName = proxyObject.getClass().getSuperclass().getName();
      Class<?>[] proxyInterfaceClasses = proxyObject.getClass().getInterfaces();
      proxyInterfaces = new ArraySet<String>(proxyInterfaceClasses.length);
      for (int i = 0; i < proxyInterfaceClasses.length; i++)
      {
         proxyInterfaces.add(proxyInterfaceClasses[i].getName());
      }
      proxyInterfaces.add(proxySuperClassName);
      this.proxyObject = proxyObject;
   }

   /**
    * Writes this object to the stream and also appends the serialization of the
    * proxy object afterwards. This allows this wrapper to later recover the
    * proxy class before trying to deserialize the proxy object.
    * 
    * @param out the output stream of objects
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
      writeProxy = true;
      out.writeUnshared(this);
   }

   /**
    * First reads the state of this object from the stream, then generates the
    * proxy class if needed, and then deserializes the proxy object.
    * 
    * @param in the object input stream
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      Class<?> proxyBeanType = Container.instance().services().get(ProxyServices.class).loadProxySuperClass(proxySuperClassName);
      ArraySet<Type> proxyBeanInterfaces = loadInterfaces(); 
      Class<?> proxyClass = null;
      if (proxyClassName.endsWith(ProxyFactory.PROXY_SUFFIX))
      {
         proxyClass = generateClientProxyClass(proxyBeanType, proxyBeanInterfaces);
      }
      else
      {
         // All other proxy classes always exist where a Weld container was deployed
         proxyClass = Container.instance().services().get(ProxyServices.class).getClassLoader(proxyBeanType).loadClass(proxyClassName);
      }
      try
      {
         proxyObject = proxyClass.getDeclaredMethod("deserializeProxy", ObjectInputStream.class).invoke(null, in);
      }
      catch (Exception e)
      {
         throw new WeldException(PROXY_DESERIALIZATION_FAILURE, e);
      }
   }

   private ArraySet<Type> loadInterfaces() throws ClassNotFoundException
   {
      ProxyServices proxyServices = Container.instance().services().get(ProxyServices.class);
      ArraySet<Type> interfaceClasses = new ArraySet<Type>(proxyInterfaces.size());
      for (String interfaceName : proxyInterfaces)
      {
         interfaceClasses.add(proxyServices.loadProxySuperClass(interfaceName));
      }
      return interfaceClasses;
   }

   /**
    * Always returns the original proxy object that was serialized.
    * 
    * @return the proxy object
    * @throws ObjectStreamException
    */
   Object readResolve() throws ObjectStreamException
   {
      return proxyObject;
   }

   Object writeReplace() throws ObjectStreamException
   {
      return writeProxy ? proxyObject : this;
   }

   private <T> Class<?> generateClientProxyClass(Class<T> beanType, Set<Type> interfaces)
   {
      return new ProxyFactory<T>(beanType, interfaces).getProxyClass();
   }
}
