package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.version") 
@LocaleData({
   @Locale("en") 
})
/**
 * Log messages for bootstrap
 * 
 * Message Ids: 000900 - 000999
 * 
 */
public enum VersionMessage
{
   
   @MessageId("000900") VERSION,

}
