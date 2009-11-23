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

   @MessageId("000700") NOT_STARTING,
   @MessageId("000701") CONTEXT_NULL,
   @MessageId("000702") BEAN_MANAGER_NOT_FOUND,
   @MessageId("000703") REQUEST_SCOPE_BEAN_STORE_MISSING,
   @MessageId("000704") BEAN_DEPLOYMENT_ARCHIVE_MISSING,
   @MessageId("000705") BEAN_MANAGER_FOR_ARCHIVE_NOT_FOUND,
   @MessageId("000706") ILLEGAL_USE_OF_WELD_LISTENER,
   @MessageId("000707") ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED;
   
}
