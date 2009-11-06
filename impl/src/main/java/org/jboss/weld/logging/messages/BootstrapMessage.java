package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.bootstrap") 
@LocaleData({
   @Locale("en") 
})
/**
 * Log messages for bootstrap
 * 
 * Message Ids: 000100 - 000199
 * 
 */
public enum BootstrapMessage
{
   
   @MessageId("000100") VALIDATING_BEANS,
   @MessageId("000101") JTA_UNAVAILABLE,
   @MessageId("000103") ENABLED_POLICIES,
   @MessageId("000104") ENABLED_DECORATORS,
   @MessageId("000105") ENABLED_INTERCEPTORS,
   @MessageId("000106") FOUND_BEAN,
   @MessageId("000107") FOUND_INTERCEPTOR,
   @MessageId("000108") FOUND_DECORATOR,
   @MessageId("000109") FOUND_OBSERVER_METHOD;

}
