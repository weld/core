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

@BaseName("org.jboss.weld.messages.bootstrap")
@LocaleData({
        @Locale("en")
})
/**
 * Log messages for bootstrap
 *
 * Message Ids: 000100 - 000199
 *
 */
public enum BootstrapMessage {

    @MessageId("000100")VALIDATING_BEANS,
    @MessageId("000101")JTA_UNAVAILABLE,
    @MessageId("000103")ENABLED_ALTERNATIVES,
    @MessageId("000104")ENABLED_DECORATORS,
    @MessageId("000105")ENABLED_INTERCEPTORS,
    @MessageId("000106")FOUND_BEAN,
    @MessageId("000107")FOUND_INTERCEPTOR,
    @MessageId("000108")FOUND_DECORATOR,
    @MessageId("000109")FOUND_OBSERVER_METHOD,
    @MessageId("000110")ANNOTATION_TYPE_NULL,
    @MessageId("000111")BEAN_TYPE_NOT_EJB,
    @MessageId("000112")BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR,
    @MessageId("000113")DEPLOYMENT_ARCHIVE_NULL,
    @MessageId("000114")DEPLOYMENT_REQUIRED,
    @MessageId("000115")BEAN_STORE_MISSING,
    @MessageId("000116")MANAGER_NOT_INITIALIZED,
    @MessageId("000117")UNSPECIFIED_REQUIRED_SERVICE,
    @MessageId("000118")PASSIVATING_NON_NORMAL_SCOPE_ILLEGAL,
    @MessageId("000119")IGNORING_CLASS_DUE_TO_LOADING_ERROR,
    @MessageId("000120")ENUMS_ALREADY_INJECTED,
    @MessageId("000122")ENUM_INJECTION_TARGET_NOT_CREATED,
    @MessageId("000123")ERROR_LOADING_BEANS_XML_ENTRY,
    @MessageId("000124")THREADS_IN_USE,
    @MessageId("000125")INVALID_THREAD_POOL_SIZE,
    @MessageId("000126")TIMEOUT_SHUTTING_DOWN_THREAD_POOL,
    @MessageId("000127")INVALID_THREAD_POOL_TYPE,
    @MessageId("000128")INVALID_PROPERTY_VALUE,
    @MessageId("000129")DUPLICATE_ANNOTATED_TYPE_ID,
    @MessageId("000130")ANNOTATED_TYPE_JAVA_CLASS_MISMATCH,
    @MessageId("000131")PRIORITY_OUTSIDE_OF_RECOMMENDED_RANGE,
    ;

}
