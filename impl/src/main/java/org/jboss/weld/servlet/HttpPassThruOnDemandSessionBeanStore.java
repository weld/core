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

package org.jboss.weld.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * A BeanStore that passes through all attributes to a session that is only
 * created on demand. In order to support on demand session creation, this
 * BeanStore must be attached to a HttpServletRequest.
 * 
 * @author David Allen
 */
public class HttpPassThruOnDemandSessionBeanStore extends HttpPassThruSessionBeanStore
{
   private final HttpServletRequest request;

   protected HttpPassThruOnDemandSessionBeanStore(HttpServletRequest request)
   {
      this.request = request;
      if (request.getSession(false) != null)
      {
         attachToSession(request.getSession());
      }
   }

   @Override
   protected void setAttribute(String key, Object instance)
   {
      if (!isAttachedToSession())
      {
         attachToSession(request.getSession(true));
      }
      super.setAttribute(key, instance);
   }

   public static HttpPassThruSessionBeanStore of(HttpServletRequest request)
   {
      return new HttpPassThruOnDemandSessionBeanStore(request);
   }
}
