/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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
package org.jboss.weld.conversation;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.servlet.ConversationBeanStore;

/**
 * The HTTP session based conversation manager
 * 
 * @author Nicklas Karlsson
 * 
 */
@SessionScoped
public class ServletConversationManager extends AbstractConversationManager
{
   private static final long serialVersionUID = 1647848566880659085L;

   private static final long CONVERSATION_TIMEOUT_IN_MS = 10 * 60 * 1000;
   private static final long CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS = 1 * 1000;
   private static final String CONVERSATION_ID_NAME = "cid";
   
   @Inject 
   private Instance<HttpSession> httpSession;

   @Override
   public BeanStore getBeanStore(String cid)
   {
      return new ConversationBeanStore(httpSession.get(), cid);
   }
   
   @Produces
   @ConversationInactivityTimeout
   public static long getConversationTimeoutInMilliseconds()
   {
      return CONVERSATION_TIMEOUT_IN_MS;
   }

   @Produces
   @ConversationConcurrentAccessTimeout
   public static long getConversationConcurrentAccessTimeout()
   {
      return CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS;
   }

   @Produces
   @ConversationIdName
   public static String getConversationIdName()
   {
      return CONVERSATION_ID_NAME;
   }
   
}
