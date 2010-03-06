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

@BaseName("org.jboss.weld.messages.conversation") 
@LocaleData({
   @Locale("en") 
})
/**
 * Log messages for conversations
 * 
 * Message Ids: 000300 - 000399
 * 
 */
public enum ConversationMessage
{

   @MessageId("000300") NO_CONVERSATION_TO_RESTORE,
   @MessageId("000301") UNABLE_TO_RESTORE_CONVERSATION,
   @MessageId("000302") CONVERSATION_LOCK_UNAVAILABLE,
   @MessageId("000303") CONVERSATION_SWITCHED,
   @MessageId("000304") CLEANING_UP_CONVERSATION,
   @MessageId("000305") CONVERSATION_TERMINATION_SCHEDULED,
   @MessageId("000306") CONVERSATION_TERMINATION_CANCELLED,
   @MessageId("000307") CONVERSATION_TERMINATION_CANCELLATION_FAILED,
   @MessageId("000308") DESTROY_LRC_COMPLETE,
   @MessageId("000309") LRC_COUNT,
   @MessageId("000310") DESTROY_TRANSIENT_COVERSATION,
   @MessageId("000311") DESTROY_LRC,
   @MessageId("000312") DESTROY_ALL_LRC,
   @MessageId("000313") CONVERSATION_LOCKED,
   @MessageId("000314") CONVERSATION_UNLOCKED,
   @MessageId("000315") CONVERSATION_UNAVAILBLE,
   @MessageId("000316") ILLEGAL_CONVERSATION_UNLOCK_ATTEMPT,
   @MessageId("000317") PROMOTED_TRANSIENT,
   @MessageId("000318") DEMOTED_LRC,
   @MessageId("000319") SWITCHED_CONVERSATION,
   @MessageId("000214") BEGIN_CALLED_ON_LONG_RUNNING_CONVERSATION,
   @MessageId("000215") END_CALLED_ON_TRANSIENT_CONVERSATION,
   @MessageId("000216") NULL_HTTP_SESSION,
   @MessageId("000217") SWITCHING_MODE_RESETS_TIMEOUTS;

}
