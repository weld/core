/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

@BaseName("org.jboss.weld.messages.beanmanager")
@LocaleData({
   @Locale("en")
})
/**
 * Log messages for bean manager and related support classes.
 * 
 * Message IDs: 001300 - 001399
 * 
 * @author David Allen
 *
 */
public enum BeanManagerMessage
{
   @MessageId("001300") CANNOT_LOCATE_BEAN_MANAGER,
   @MessageId("001301") INVALID_QUALIFIER,
   @MessageId("001302") DUPLICATE_QUALIFIERS,
   @MessageId("001303") CONTEXT_NOT_ACTIVE,
   @MessageId("001304") DUPLICATE_ACTIVE_CONTEXTS,
   @MessageId("001305") SPECIFIED_TYPE_NOT_BEAN_TYPE,
   @MessageId("001306") UNPROXYABLE_RESOLUTION,
   @MessageId("001307") UNRESOLVABLE_TYPE,
   @MessageId("001308") UNRESOLVABLE_ELEMENT,
   @MessageId("001309") NOT_PROXYABLE,
   @MessageId("001310") NO_DECORATOR_TYPES,
   @MessageId("001311") INTERCEPTOR_BINDINGS_EMPTY,
   @MessageId("001312") DUPLICATE_INTERCEPTOR_BINDING,
   @MessageId("001313") INTERCEPTOR_RESOLUTION_WITH_NONBINDING_TYPE,
   @MessageId("001314") NON_NORMAL_SCOPE,
   @MessageId("001315") TOO_MANY_ACTIVITIES,
   @MessageId("001316") NOT_INTERCEPTOR_BINDING_TYPE,
   @MessageId("001317") NOT_STEREOTYPE,
   @MessageId("001318") AMBIGUOUS_BEANS_FOR_DEPENDENCY,
   @MessageId("001319") NULL_BEAN_MANAGER_ID,
   @MessageId("001320") INJECTION_ON_NON_CONTEXTUAL,
   @MessageId("001321") MISSING_BEAN_CONSTRUCTOR_FOUND,
   @MessageId("001322") ERROR_INVOKING_POST_CONSTRUCT,
   @MessageId("001323") ERROR_INVOKING_PRE_DESTROY;
}
