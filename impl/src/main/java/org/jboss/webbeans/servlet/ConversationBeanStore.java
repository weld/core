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
package org.jboss.webbeans.servlet;

import javax.servlet.http.HttpSession;

import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.context.beanstore.BeanStoreNamingScheme;
import org.jboss.webbeans.context.beanstore.PrefixBeanStoreNamingScheme;

/**
 * A HTTP session backed bean store for the conversational scope
 * 
 * @author Nicklas Karlsson
 */
public class ConversationBeanStore extends HttpSessionBeanStore
{
   private String cid;

   public ConversationBeanStore(HttpSession session, String cid)
   {
      super(session);
      this.cid = cid;
   }

   @Override
   protected BeanStoreNamingScheme getNamingScheme()
   {
      return new PrefixBeanStoreNamingScheme(ConversationContext.class.getName() + "[" + cid + "]", "#");
   }

}
