/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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
package org.jboss.webbeans.conversation;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ConversationBeanStore;

/**
 * The HTTP session based conversation manager
 * 
 * @author Nicklas Karlsson
 * 
 */
@SessionScoped
public class ServletConversationManager extends AbstractConversationManager implements Serializable
{
   private static final long serialVersionUID = 1647848566880659085L;

   private static LogProvider log = Logging.getLogProvider(ServletConversationManager.class);

   private static final long CONVERSATION_TIMEOUT_IN_MS = 10 * 60 * 1000;
   private static final long CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS = 1 * 1000;
   private static final String CONVERSATION_ID_NAME = "cid";
   
   @Inject Instance<HttpSession> httpSession;

   @Override
   public BeanStore getBeanStore(String cid)
   {
      return new ConversationBeanStore(httpSession.get(), cid);
   }
   
   @Produces
   @ConversationInactivityTimeout
   public static long getConversationTimeoutInMilliseconds()
   {
      log.trace("Produced conversation timeout " + CONVERSATION_TIMEOUT_IN_MS);
      return CONVERSATION_TIMEOUT_IN_MS;
   }

   @Produces
   @ConversationConcurrentAccessTimeout
   public static long getConversationConcurrentAccessTimeout()
   {
      log.trace("Produced conversation concurrent access timeout " + CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS);
      return CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS;
   }

   @Produces
   @ConversationIdName
   public static String getConversationIdName()
   {
      log.trace("Produced conversation id name " + CONVERSATION_ID_NAME);
      return CONVERSATION_ID_NAME;
   }
   
}
