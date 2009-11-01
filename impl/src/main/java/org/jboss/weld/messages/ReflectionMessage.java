package org.jboss.weld.messages;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.reflection")
@LocaleData({
   @Locale("en")
})
public enum ReflectionMessage
{

   MISSING_RETENTION,
   MISSING_TARGET,
   MISSING_TARGET_TYPE_METHOD_OR_TARGET_TYPE,
   TARGET_TYPE_METHOD_INHERITS_FROM_TARGET_TYPE,
   MISSING_TARGET_METHOD_FIELD_TYPE,
   MISSING_TARGET_METHOD_FIELD_TYPE_PARAMETER_OR_TARGET_METHOD_TYPE_OR_TARGET_METHOD_OR_TARGET_TYPE_OR_TARGET_FIELD
   
}
