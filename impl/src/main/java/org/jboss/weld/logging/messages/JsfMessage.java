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
   @MessageId("000504") RESUMING_CONVERSATION;
   
}
