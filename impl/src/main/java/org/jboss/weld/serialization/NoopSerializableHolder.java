/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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


/**
 * The default implementation. The object is serializable if and only if the value is serializable.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class NoopSerializableHolder<T> implements SerializableHolder<T> {

    public static <T> NoopSerializableHolder<T> of(T value) {
        return new NoopSerializableHolder<T>(value);
    }

    private static final long serialVersionUID = -6518106809153308224L;

    T value;

    public NoopSerializableHolder(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
