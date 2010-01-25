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
   @MessageId("000609") CLEANING_JAVASSIST_PROXY_CLASS,
   @MessageId("000610") UNABLE_TO_GET_CONSTRUCTOR_ON_DESERIALIZATION,
   @MessageId("000611") UNABLE_TO_GET_METHOD_ON_DESERIALIZATION,
   @MessageId("000612") UNABLE_TO_GET_FIELD_ON_DESERIALIZATION,
   @MessageId("000613") UNABLE_TO_GET_PARAMETER_ON_DESERIALIZATION;
   
}
