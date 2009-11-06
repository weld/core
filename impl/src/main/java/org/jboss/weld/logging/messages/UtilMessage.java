package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.util")
@LocaleData({
   @Locale("en")
})
/**
 * Error messages relating to utility classes
 * 
 * TODO maybe remove this, I'm not sure users care what we count a utility PLM
 * 
 * Message ids: 000800 - 000899
 */
public enum UtilMessage
{

   @MessageId("000800") SERVICE_LOADER_LOADING_ERROR,
   @MessageId("000801") SECURITY_EXCEPTION_SCANNING;
   
}
