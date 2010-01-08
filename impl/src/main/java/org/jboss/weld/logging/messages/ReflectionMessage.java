package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.reflection") 
@LocaleData({
   @Locale("en") 
})
/**
 * Log messages relating to reflection
 * 
 * Message ids: 000600 - 000699
 * 
 */
public enum ReflectionMessage
{

   @MessageId("000600") MISSING_RETENTION,
   @MessageId("000601") MISSING_TARGET,
   @MessageId("000602") MISSING_TARGET_TYPE_METHOD_OR_TARGET_TYPE,
   @MessageId("000603") TARGET_TYPE_METHOD_INHERITS_FROM_TARGET_TYPE,
   @MessageId("000604") MISSING_TARGET_METHOD_FIELD_TYPE,
   @MessageId("000605") MISSING_TARGET_METHOD_FIELD_TYPE_PARAMETER_OR_TARGET_METHOD_TYPE_OR_TARGET_METHOD_OR_TARGET_TYPE_OR_TARGET_FIELD,
   @MessageId("000606") UNABLE_TO_GET_PARAMETER_NAME,
   @MessageId("000607") ANNOTATION_MAP_NULL,
   @MessageId("000608") DECLARED_ANNOTATION_MAP_NULL,
   @MessageId("000609") CLEANING_JAVASSIST_PROXY_CLASS;
   
}
