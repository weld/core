/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.tests.util;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.container.ResourceContainer;

/**
 * Builder for properties files assets.
 *
 * @author Tomas Remes
 */
public class PropertiesBuilder {

    private StringBuilder stringBuilder;

    private PropertiesBuilder() {
        stringBuilder = new StringBuilder();
    }

    /**
     * @return a new builder instance
     */
    public static PropertiesBuilder newBuilder() {
        return new PropertiesBuilder();
    }

    /**
     * @param key property key
     * @param value property value
     * @return self
     */
    public PropertiesBuilder set(String key, String value) {
        stringBuilder.append(key);
        stringBuilder.append("=");
        stringBuilder.append(value);
        stringBuilder.append('\n');
        return this;
    }

    /**
     * @return new string asset instance
     */
    public StringAsset build() {
        return new StringAsset(stringBuilder.toString());
    }

    /**
     * @param archive deployment archive in which the property file is added
     */
    public void addAsSystemProperties(ResourceContainer<?> archive) {
        archive.addAsResource(new StringAsset(stringBuilder.toString()), SystemPropertiesLoader.PROPERTIES_FILE_NAME);
    }

}
