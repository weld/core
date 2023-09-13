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
package org.jboss.weld.config;

import static org.jboss.weld.config.WeldConfiguration.checkRequiredType;
import static org.jboss.weld.config.WeldConfiguration.getSystemProperty;

/**
 * If a configuration property needs to be accessed before the Weld container initializes, the system property is so far the
 * only option.
 *
 * @author Martin Kouba
 * @see ConfigurationKey
 */
public final class SystemPropertiesConfiguration {

    public static final SystemPropertiesConfiguration INSTANCE = new SystemPropertiesConfiguration();

    private boolean xmlValidationDisabled;

    private SystemPropertiesConfiguration() {
        xmlValidationDisabled = initSystemProperty(ConfigurationKey.DISABLE_XML_VALIDATION, Boolean.class);
    }

    /**
     * XML descriptor validation is enabled by default.
     *
     * @return <code>true</code> if the validation is disabled, <code>false</code> otherwise
     */
    public boolean isXmlValidationDisabled() {
        return xmlValidationDisabled;
    }

    @SuppressWarnings("unchecked")
    private <T> T initSystemProperty(ConfigurationKey key, Class<T> requiredType) {
        checkRequiredType(key, requiredType);
        String property = getSystemProperty(key.get());
        return (T) (property != null ? key.convertValue(property) : key.getDefaultValue());
    }

}
