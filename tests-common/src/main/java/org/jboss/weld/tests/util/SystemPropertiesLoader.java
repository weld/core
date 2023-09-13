/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.weld.tests.util;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import org.jboss.weld.config.ConfigurationKey;

/**
 * Reads system.properties file from test deployment and sets/unsets its content as a system properties
 */

@Startup
@Singleton
public class SystemPropertiesLoader {

    public static final String PROPERTIES_FILE_NAME = "system.properties";

    @PostConstruct
    public void setProperties() {

        Properties props = load(PROPERTIES_FILE_NAME);
        if (props != null) {
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                System.setProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    @PreDestroy
    public void unsetProperties() {
        Properties props = load(PROPERTIES_FILE_NAME);
        if (props != null) {
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                if (ConfigurationKey.fromString(entry.getKey().toString()) != null) {
                    System.setProperty(entry.getKey().toString(),
                            ConfigurationKey.fromString(entry.getKey().toString()).getDefaultValue().toString());
                } else {
                    System.clearProperty(entry.getKey().toString());
                }
            }
        }
    }

    private Properties load(String resource) {
        InputStream propsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (propsStream != null) {
            Properties props = new Properties();
            try {
                props.load(propsStream);
                return props;
            } catch (Exception e) {
                throw new RuntimeException("Could not load properties", e);
            }
        }
        return null;
    }
}
