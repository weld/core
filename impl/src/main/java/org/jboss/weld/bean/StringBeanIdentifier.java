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
package org.jboss.weld.bean;

import org.jboss.weld.serialization.spi.BeanIdentifier;

public class StringBeanIdentifier implements BeanIdentifier {

    private static final long serialVersionUID = -3389031898783605246L;

    private final String value;

    public StringBeanIdentifier(String value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StringBeanIdentifier) {
            StringBeanIdentifier that = (StringBeanIdentifier) obj;
            return this.value.equals(that.value);
        }
        if (obj instanceof BeanIdentifier) {
            BeanIdentifier that = (BeanIdentifier) obj;
            return this.value.equals(that.asString());
        }
        return false;
    }

    @Override
    public String toString() {
        return asString();
    }
}
