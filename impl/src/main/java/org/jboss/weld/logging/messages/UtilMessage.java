/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.logging.messages;

import org.jboss.weld.logging.MessageId;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("org.jboss.weld.messages.util")
@LocaleData({
   @Locale("en")
})
/**
 * Error messages relating to utility classes
 * 
 * TODO maybe remove this, I'm not sure users care what we count a utility PLM
 * 
 * Message ids: 000800 - 000899
 */
public enum UtilMessage
{

   @MessageId("000800") SERVICE_LOADER_LOADING_ERROR,
   @MessageId("000801") SECURITY_EXCEPTION_SCANNING,
   @MessageId("000802") XML_DOM_READONLY,
   @MessageId("000803") DECLARED_EXTENSION_DOES_NOT_IMPLEMENT_EXTENSION,
   @MessageId("000804") CLASS_NOT_ENUM,
   @MessageId("000805") TOO_MANY_POST_CONSTRUCT_METHODS,
   @MessageId("000806") TOO_MANY_PRE_DESTROY_METHODS,
   @MessageId("000807") INITIALIZER_CANNOT_BE_PRODUCER,
   @MessageId("000808") INITIALIZER_CANNOT_BE_DISPOSAL_METHOD,
   @MessageId("000810") QUALIFIER_ON_FINAL_FIELD,
   @MessageId("000811") TOO_MANY_INITIALIZERS,
   @MessageId("000812") AMBIGUOUS_CONSTRUCTOR,
   @MessageId("000813") INVALID_QUANTITY_INJECTABLE_FIELDS_AND_INITIALIZER_METHODS,
   @MessageId("000814") ANNOTATION_NOT_QUALIFIER,
   @MessageId("000815") REDUNDANT_QUALIFIER,
   @MessageId("000816") UNABLE_TO_FIND_CONSTRUCTOR,
   @MessageId("000817") UNABLE_TO_FIND_BEAN_DEPLOYMENT_ARCHIVE,
   @MessageId("000818") EVENT_TYPE_NOT_ALLOWED,
   @MessageId("000819") TYPE_PARAMETER_NOT_ALLOWED_IN_EVENT_TYPE,
   @MessageId("000820") CANNOT_PROXY_NON_CLASS_TYPE,
   @MessageId("000821") INSTANCE_NOT_A_PROXY,
   @MessageId("000822") ACCESS_ERROR_ON_CONSTRUCTOR,
   @MessageId("000823") ERROR_INVOKING_METHOD,
   @MessageId("000824") ACCESS_ERROR_ON_FIELD,
   @MessageId("000825") NO_SUCH_METHOD,
   @MessageId("000826") ANNOTATION_VALUES_INACCESSIBLE,
   @MessageId("000827") INITIALIZER_METHOD_IS_GENERIC,
   @MessageId("000827") COULD_NOT_READ_SERVICES_LIST,
   @MessageId("000828") COULD_NOT_READ_SERVICES_FILE,
   @MessageId("000829") EXTENSION_CLASS_NOT_FOUND;
   
}
