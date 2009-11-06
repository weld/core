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

   @MessageId("000700") NOT_STARTING;
   
}
