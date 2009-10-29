package org.jboss.weld.messages;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.bootstrap")
@LocaleData({
   @Locale("en")
})
public enum BootstrapMessages
{
   
   VALIDATING_BEANS,
   JTA_UNAVAILABLE,
   VERSION,
   ENABLED_POLICIES,
   ENABLED_DECORATORS,
   ENABLED_INTERCEPTORS,
   FOUND_BEAN,
   FOUND_INTERCEPTOR,
   FOUND_DECORATOR,
   FOUND_OBSERVER_METHOD,

}
