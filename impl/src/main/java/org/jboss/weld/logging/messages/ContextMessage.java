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
