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

import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.serialization.spi.BeanIdentifier;

public class ManagedBeanIdentifier implements BeanIdentifier {

    private static final long serialVersionUID = -2549776947566879012L;

    private final AnnotatedTypeIdentifier typeIdentifier;
    private final int hashCode;

    public ManagedBeanIdentifier(AnnotatedTypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
        this.hashCode = asString().hashCode();
    }

    @Override
    public String asString() {
        return BeanIdentifiers.forManagedBean(typeIdentifier);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BeanIdentifier) {
            if (this.hashCode != obj.hashCode()) {
                return false;
            }
            if (obj instanceof ManagedBeanIdentifier) {
                ManagedBeanIdentifier that = (ManagedBeanIdentifier) obj;
                return this.typeIdentifier.equals(that.typeIdentifier);
            }
            BeanIdentifier that = (BeanIdentifier) obj;
            return this.asString().equals(that.asString());
        }
        return false;
    }

    @Override
    public String toString() {
        return asString();
    }
}
