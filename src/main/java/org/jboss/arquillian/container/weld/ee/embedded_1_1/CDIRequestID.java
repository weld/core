/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import org.jboss.weld.context.api.BeanStore;

/**
 * Simple CDI COnversation ID holder object. Bound to the Class context
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CDIRequestID 
{
   private String id;
   private BeanStore beanStore;
   
   public CDIRequestID(final String id, BeanStore beanStore)
   {
      this.id = id;
      this.beanStore = beanStore;
   }
   
   public String getId()
   {
      return id;
   }
   
   public BeanStore getBeanStore()
   {
      return beanStore;
   }
}

