package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.event")
@LocaleData({
   @Locale("en")
})
/**
 * Log messages for events
 * 
 * Message ids: 000400 - 000499
 */
public enum EventMessage
{

   @MessageId("000400") ASYNC_FIRE,
   @MessageId("000401") ASYNC_OBSERVER_FAILURE,
   @MessageId("000402") ASYNC_TX_FIRE;
   
}
