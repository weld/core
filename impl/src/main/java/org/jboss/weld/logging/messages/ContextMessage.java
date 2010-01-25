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

@BaseName("org.jboss.weld.messages.context") 
@LocaleData({
   @Locale("en") 
})
/**
 * Log messages for Contexts
 * 
 * Message Ids: 000200 - 000299
 */
public enum ContextMessage
{
   
   @MessageId("000200") CONTEXTUAL_INSTANCE_FOUND,
   @MessageId("000201") CONTEXT_CLEARED,
   @MessageId("000202") CONTEXTUAL_INSTANCE_ADDED,
   @MessageId("000203") CONTEXTUAL_INSTANCE_REMOVED,
   @MessageId("000204") SESSION_RESTORED,
   @MessageId("000205") SESSION_ENDED,
   @MessageId("000206") REQUEST_STARTED,
   @MessageId("000207") REQUEST_ENDED,
   @MessageId("000208") APPLICATION_STARTED,
   @MessageId("000209") APPLICATION_ENDED,
   @MessageId("000210") DEPENDENT_INSTANCE_ATTACHED,
   @MessageId("000211") DELIMITER_IN_PREFIX,
   @MessageId("000212") CONTEXTUAL_IS_NULL,
   @MessageId("000213") NO_BEAN_STORE_AVAILABLE;
   
}
