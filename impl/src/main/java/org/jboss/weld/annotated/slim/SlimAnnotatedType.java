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
package org.jboss.weld.annotated.slim;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.Identified;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.resources.ClassTransformer;

/**
 * Marker interface for lightweight implementations of {@link AnnotatedType}.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type
 */
public interface SlimAnnotatedType<T> extends AnnotatedType<T>, Identified<AnnotatedTypeIdentifier> {

    /**
     * Clear up cached content to save memory. Called after bootstrap is complete.
     */
    void clear();

    public static class SerializationProxy<X> implements Serializable {
        private static final long serialVersionUID = 6800705438537396237L;
        private final AnnotatedTypeIdentifier identifier;

        public SerializationProxy(AnnotatedTypeIdentifier identifier) {
            this.identifier = identifier;
        }

        private Object readResolve() throws ObjectStreamException {
            SlimAnnotatedType<?> type = Container.instance(identifier).services().get(ClassTransformer.class).getSlimAnnotatedTypeById(identifier);
            if (type == null) {
                throw MetadataLogger.LOG.annotatedTypeDeserializationFailure(identifier);
            }
            return type;
        }
    }
}
