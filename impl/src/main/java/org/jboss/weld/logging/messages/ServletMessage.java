/*
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
package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.servlet")
@LocaleData({
   @Locale("en")
})
/**
 * Error messages relating to Servlet integration
 * 
 * Message ids: 000700 - 000799
 */
public enum ServletMessage
{

   @MessageId("000700") NOT_STARTING,
   @MessageId("000701") CONTEXT_NULL,
   @MessageId("000702") BEAN_MANAGER_NOT_FOUND,
   @MessageId("000703") REQUEST_SCOPE_BEAN_STORE_MISSING,
   @MessageId("000704") BEAN_DEPLOYMENT_ARCHIVE_MISSING,
   @MessageId("000705") BEAN_MANAGER_FOR_ARCHIVE_NOT_FOUND,
   @MessageId("000706") ILLEGAL_USE_OF_WELD_LISTENER,
   @MessageId("000707") ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED,
   @MessageId("000708") REQUEST_INITIALIZED,
   @MessageId("000709") REQUEST_DESTROYED;
   
}
