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

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;
import org.jboss.weld.logging.MessageId;

@BaseName("org.jboss.weld.messages.metadata")
@LocaleData({
        @Locale("en")
})
/**
 * Log messages for Meta Data.
 *
 * Message IDs: 001100 - 001199
 *
 * @author David Allen
 *
 */
public enum MetadataMessage {
    @MessageId("001100")META_ANNOTATION_ON_WRONG_TYPE,
    @MessageId("001101")NON_BINDING_MEMBER_TYPE,
    @MessageId("001102")STEREOTYPE_NOT_REGISTERED,
    @MessageId("001103")QUALIFIER_ON_STEREOTYPE,
    @MessageId("001104")VALUE_ON_NAMED_STEREOTYPE,
    @MessageId("001105")MULTIPLE_SCOPES,
    @MessageId("001106")STEREOTYPES_NULL,
    @MessageId("001107")QUALIFIERS_NULL,
    @MessageId("001108")TYPES_NULL,
    @MessageId("001109")SCOPE_NULL,
    @MessageId("001110")NOT_A_STEREOTYPE,
    @MessageId("001111")NOT_A_QUALIFIER,
    @MessageId("001112")TYPES_EMPTY,
    @MessageId("001113")NOT_A_SCOPE,
}
