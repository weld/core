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
package org.jboss.weld.logging;

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Log messages for configuration.
 *
 * Message IDs: 001900 - 001999
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ConfigurationLogger extends WeldLogger {

    ConfigurationLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), ConfigurationLogger.class,
            Category.CONFIGURATION.getName());

    @Message(id = 1900, value = "Invalid configuration property value {0} for key {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException invalidConfigurationPropertyValue(Object value, Object key);

    @Message(id = 1901, value = "Configuration property type {0} does not match the required type {1} for configuration key {2}", format = Format.MESSAGE_FORMAT)
    IllegalStateException configurationPropertyTypeMismatch(Object propertyType, Object requiredType, Object key);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1902, value = "Following configuration was detected and applied: {0}", format = Format.MESSAGE_FORMAT)
    void configurationInitialized(Object configuration);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1903, value = "Configuration key {0} already set to {1} in a source with higher priority, value {2} from {3} is ignored", format = Format.MESSAGE_FORMAT)
    void configurationKeyAlreadySet(Object configurationKey, Object value, Object ignoredValue, String mergedSourceDescription);

    @LogMessage(level = Level.WARN)
    @Message(id = 1904, value = "Unsupported configuration key found and ignored: {0}", format = Format.MESSAGE_FORMAT)
    void unsupportedConfigurationKeyFound(Object key);

    @Message(id = 1905, value = "Configuration key {0} set to different values in the same source:\n - {1}\n - {2}", format = Format.MESSAGE_FORMAT)
    IllegalStateException configurationKeyHasDifferentValues(Object key, Object value1, Object value2);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1906, value = "ResourceLoader not specified for {0}, file properties will not be loaded", format = Format.MESSAGE_FORMAT)
    void resourceLoaderNotSpecifiedForArchive(Object archive);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1907, value = "Reading properties file: {0}", format = Format.MESSAGE_FORMAT)
    void readingPropertiesFile(Object file);

    @LogMessage(level = Level.WARN)
    @Message(id = 1908, value = "Configuration property {0} can only be set by integrator - value {1} ignored", format = Format.MESSAGE_FORMAT)
    void cannotSetIntegratorOnlyConfigurationProperty(Object key, Object value);

}
