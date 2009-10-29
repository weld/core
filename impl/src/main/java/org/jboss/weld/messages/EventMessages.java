package org.jboss.weld.messages;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.event")
@LocaleData({
   @Locale("en")
})
public enum EventMessages
{

   ASYNC_FIRE,
   ASYNC_OBSERVER_FAILURE,
   ASYNC_TX_FIRE
   
}
