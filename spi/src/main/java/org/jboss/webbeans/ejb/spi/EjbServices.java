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

package org.jboss.webbeans.ejb.spi;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.ejb.api.SessionObjectReference;

/**
 * A container should implement this interface to allow Web Beans to
 * resolve EJB and discover EJBs
 *
 * {@link EjbServices} is a per-deployment service.
 *
 * @author Pete Muir
 * @author Marius Bogoevici
 *
 */
public interface EjbServices extends Service
{

   /**
    * Request a reference to an EJB session object from the container. If the
    * EJB being resolved is a stateful session bean, the container should ensure
    * the session bean is created before this method returns.
    *
    * @param ejbDescriptor the ejb to resolve
    * @return a reference to the session object
    */
   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor);

   /**
    * Provides interceptor binding metadata to the container. This method should be
    * called before any EJB object is created.
    *
    * @param ejbDescriptor the ejb to bound interceptors to
    * @param interceptorBindings the interceptor bindings descriptor
    */
   public void registerInterceptors(EjbDescriptor<?> ejbDescriptor, InterceptorBindings interceptorBindings);

}
