package org.jboss.weld.messages;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.context")
@LocaleData({
   @Locale("en")
})
public enum ContextMessages
{
   
   CONTEXTUAL_INSTANCE_FOUND,
   CONTEXT_CLEARED,
   CONTEXTUAL_INSTANCE_ADDED,
   CONTEXTUAL_INSTANCE_REMOVED,
   SESSION_RESTORED,
   SESSION_ENDED,
   REQUEST_STARTED,
   REQUEST_ENDED,
   APPLICATION_STARTED,
   APPLICATION_ENDED,
   DEPENDENT_INSTANCE_ATTACHED
   
}
