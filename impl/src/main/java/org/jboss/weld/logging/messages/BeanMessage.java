package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.bean")
@LocaleData({
   @Locale("en")
})
/**
 * Log messages for Beans.
 * 
 * Message IDs: 000000 - 000099
 * 
 */
public enum BeanMessage
{
   @MessageId("000000") FOUND_INJECTABLE_CONSTRUCTORS,
   @MessageId("000001") FOUND_ONE_INJECTABLE_CONSTRUCTOR,
   @MessageId("000002") FOUND_DEFAULT_CONSTRUCTOR,
   @MessageId("000003") FOUND_POST_CONSTRUCT_METHODS,
   @MessageId("000004") FOUND_ONE_POST_CONSTRUCT_METHOD,
   @MessageId("000005") FOUND_PRE_DESTROY_METHODS,
   @MessageId("000006") FOUND_ONE_PRE_DESTROY_METHOD,
   @MessageId("000007") CREATED_SESSION_BEAN_PROXY,
   @MessageId("000008") CALL_PROXIED_METHOD,
   @MessageId("000009") DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED,
   @MessageId("000010") QUALIFIERS_USED,
   @MessageId("000011") USING_DEFAULT_QUALIFIER,
   @MessageId("000012") CREATING_BEAN,
   @MessageId("000013") USING_DEFAULT_NAME,
   @MessageId("000014") USING_NAME,
   @MessageId("000015") USING_SCOPE_FROM_STEREOTYPE,
   @MessageId("000016") USING_SCOPE,
   @MessageId("000017") USING_DEFAULT_SCOPE,
   @MessageId("000018") CIRCULAR_CALL,
   @MessageId("000019") ERROR_DESTROYING,
   @MessageId("000020") USER_DEFINED_DECORATOR_DISALLOWED
   
}
