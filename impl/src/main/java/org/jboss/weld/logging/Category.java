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
package org.jboss.weld.logging;

public enum Category {

    BOOTSTRAP("Bootstrap"),
    BOOTSTRAP_TRACKER("BootstrapTracker"),
    VERSION("Version"),
    UTIL("Utilities"),
    BEAN("Bean"),
    SERVLET("Servlet"),
    REFLECTION("Reflection"),
    JSF("JSF"),
    EVENT("Event"),
    CONVERSATION("Conversation"),
    CONTEXT("Context"),
    EL("El"),
    RESOLUTION("Resolution"),
    BEAN_MANAGER("BeanManager"),
    VALIDATOR("Validator"),
    INTERCEPTOR("Interceptor"),
    SERIALIZATION("Serialization"),
    CONFIGURATION("Configuration"),
    LITE_EXTENSION_TRANSLATOR("LiteExtensionTranslator"),
    INVOKER("Invoker"),
    ;

    private static final String LOG_PREFIX = "org.jboss.weld.";

    private final String name;

    Category(String name) {
        this.name = createName(name);
    }

    public String getName() {
        return name;
    }

    private static String createName(String name) {
        return new StringBuilder().append(LOG_PREFIX).append(name).toString();
    }

}
