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

@BaseName("org.jboss.weld.messages.xml")
@LocaleData({
   @Locale("en")
})
/**
 * Error messages relating to XML parser
 * 
 * Message ids: 001200 - 001299
 */
public enum XmlMessage
{
   @MessageId("001200") CONFIGURATION_ERROR,
   @MessageId("001201") LOAD_ERROR,
   @MessageId("001202") PARSING_ERROR,
   @MessageId("001203") MULTIPLE_ALTERNATIVES,
   @MessageId("001204") MULTIPLE_DECORATORS,
   @MessageId("001205") MULTIPLE_INTERCEPTORS,
   @MessageId("001206") CANNOT_LOAD_CLASS,
   @MessageId("001207") MULTIPLE_SCANNING;

}
