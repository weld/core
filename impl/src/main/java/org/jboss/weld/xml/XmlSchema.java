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
package org.jboss.weld.xml;

import jakarta.enterprise.inject.spi.BeanManager;

public enum XmlSchema {

    CDI10("beans_1_0.xsd", BeanManager.class.getClassLoader()),
    CDI11("beans_1_1.xsd", BeanManager.class.getClassLoader()),
    CDI20("beans_2_0.xsd",
            BeanManager.class.getClassLoader()),
    WELD11("weld_1_1.xsd", BeansXmlStreamParser.class.getClassLoader());

    static final XmlSchema[] CDI11_SCHEMAS = { CDI10, WELD11, CDI11 };

    static final XmlSchema[] CDI20_SCHEMAS = { CDI10, WELD11, CDI20 };

    private final String fileName;
    private final transient ClassLoader classLoader;

    private XmlSchema(String fileName, ClassLoader classLoader) {
        this.fileName = fileName;
        this.classLoader = classLoader;
    }

    public String getFileName() {
        return fileName;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
