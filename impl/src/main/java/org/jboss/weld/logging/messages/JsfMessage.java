/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

@BaseName("org.jboss.weld.messages.jsf") 
@LocaleData({
   @Locale("en") 
})
/**
 * Log messages for JSF integration
 * 
 * Message Ids: 000500 - 000599
 */
public enum JsfMessage
{

   @MessageId("000500") CLEANING_UP_CONVERSATION,
   @MessageId("000501") SKIPPING_CLEANING_UP_CONVERSATION,
   @MessageId("000502") INITIATING_CONVERSATION,
   @MessageId("000503") FOUND_CONVERSATION_FROM_REQUEST,
   @MessageId("000504") RESUMING_CONVERSATION,
   @MessageId("000505") IMPROPER_ENVIRONMENT;
   
}
