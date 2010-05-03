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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.serialization.spi.ProxyServices;

/**
 * A wrapper mostly for client proxies which provides header information
 * useful to generate the client proxy class in a VM before the proxy
 * object is deserialized.  Only client proxies really need this
 * extra step for serialization and deserialization since the other
 * proxy classes are generated during bean archive deployment.
 * 
 * @author David Allen
 */
public class SerializableProxy implements Serializable
{

   private static final long serialVersionUID = -7682006876407447753L;

   // Information required to generate client proxy classes
   private final String proxyClassName;
   private final String proxySuperClassName;

   // The wrapped proxy object not serialized by default actions
   private transient Object proxyObject;

   public SerializableProxy(Object proxyObject)
   {
      if (!ProxyFactory.isProxy(proxyObject))
      {
         throw new ForbiddenStateException(BeanMessage.PROXY_REQUIRED);
      }
      this.proxyClassName = proxyObject.getClass().getName();
      this.proxySuperClassName = proxyObject.getClass().getSuperclass().getName();
      this.proxyObject = proxyObject;
   }

   /**
    * Writes this object to the stream and also appends the serialization of
    * the proxy object afterwards.  This allows this wrapper to later recover
    * the proxy class before trying to deserialize the proxy object.
    * 
    * @param out the output stream of objects
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
      // Must use another OO stream since the proxy was replaced in the original
      ObjectOutputStream out2 = new ObjectOutputStream(out);
      out2.writeObject(proxyObject);
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
      // Must use another OO stream per writeObject() above
      ObjectInputStream in2 = new ObjectInputStream(in);
      Class<?> proxyBeanType = Container.instance().services().get(ProxyServices.class).loadProxySuperClass(proxySuperClassName);
      if (proxyClassName.endsWith(ProxyFactory.PROXY_SUFFIX))
      {
         generateClientProxyClass(proxyBeanType);
      }
      proxyObject = in2.readObject();
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
   
   private <T> void generateClientProxyClass(Class<T> beanType)
   {
      new ProxyFactory<T>(beanType).getProxyClass();
   }
}
