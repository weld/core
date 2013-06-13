/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld;

import java.security.AccessController;

import org.jboss.weld.security.GetBooleanSystemPropertyAction;

/**
 *
 * @author Martin Kouba
 */
public final class SystemPropertiesConfiguration {

    public static final String DISABLE_XML_VALIDATION_KEY = "org.jboss.weld.xml.disableValidating";

    public static final String NON_PORTABLE_MODE_KEY = "org.jboss.weld.nonPortableMode";

    public static final SystemPropertiesConfiguration INSTANCE = new SystemPropertiesConfiguration();

    private boolean xmlValidationDisabled;

    private boolean nonPortableModeEnabled;

    private SystemPropertiesConfiguration() {
        xmlValidationDisabled = initBooleanSystemProperty(DISABLE_XML_VALIDATION_KEY, false);
        nonPortableModeEnabled = initBooleanSystemProperty(NON_PORTABLE_MODE_KEY, false);
    }

    /**
     * XML descriptor validation is enabled by default.
     *
     * @return <code>true</code> if the validation is disabled, <code>false</code> otherwise
     */
    public boolean isXmlValidationDisabled() {
        return xmlValidationDisabled;
    }

    /**
     * The non-portable mode is disabled by default.
     *
     * @return <code>true</code> if the non-portable mode is enabled, <code>false</code> otherwise
     */
    public boolean isNonPortableModeEnabled() {
        return nonPortableModeEnabled;
    }

    private boolean initBooleanSystemProperty(String key, boolean defaultValue) {
        try {
            return AccessController.doPrivileged(new GetBooleanSystemPropertyAction(key));
        } catch (Throwable ignore) {
            return defaultValue;
        }
    }

}
