/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.serialization;

import java.lang.reflect.Field;

import org.jboss.weld.logging.ReflectionLogger;

/**
 * Serializable holder for {@link Field}.
 *
 * @author Jozef Hartinger
 *
 */
public class FieldHolder extends AbstractSerializableHolder<Field> {

    private static final long serialVersionUID = 407021346356682729L;

    private final Class<?> declaringClass;
    private final String fieldName;

    public FieldHolder(Field field) {
        super(field);
        this.declaringClass = field.getDeclaringClass();
        this.fieldName = field.getName();
    }

    @Override
    protected Field initialize() {
        try {
            return declaringClass.getDeclaredField(fieldName);
        } catch (Exception e) {
            throw ReflectionLogger.LOG.unableToGetFieldOnDeserialization(declaringClass, fieldName, e);
        }
    }
}
