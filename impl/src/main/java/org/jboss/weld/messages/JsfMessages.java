package org.jboss.weld.messages;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.jsf")
@LocaleData({
   @Locale("en")
})
public enum JsfMessages
{

   CLEANING_UP_CONVERSATION,
   SKIPPING_CLEANING_UP_CONVERSATION,
   INITIATING_CONVERSATION,
   FOUND_CONVERSATION_FROM_REQUEST,
   RESUMING_CONVERSATION
   
}
