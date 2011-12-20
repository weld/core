/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.introspector.ConstructorSignature;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Serializable holder for a constructor that has been discovered an thus can be recreated from the class definition.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class DiscoveredWeldConstructorSerializableHolder<T> extends AbstractWeldAnnotatedHolder<T> implements SerializableHolder<WeldConstructor<T>> {

    public static <T> DiscoveredWeldConstructorSerializableHolder<T> of(WeldConstructor<T> constructor) {
        return new DiscoveredWeldConstructorSerializableHolder<T>(constructor);
    }

    private static final long serialVersionUID = -3994479067557140156L;

    private final ConstructorSignature signature;
    private transient WeldConstructor<T> constructor;

    public DiscoveredWeldConstructorSerializableHolder(WeldConstructor<T> constructor) {
        super(constructor.getDeclaringType().getJavaClass());
        this.constructor = constructor;
        this.signature = constructor.getSignature();
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        is.defaultReadObject();
        this.constructor = Reflections.cast(getDeclaringWeldClass().getDeclaredWeldConstructor(signature));
        if (constructor == null) {
            throw new IllegalStateException(ReflectionMessage.UNABLE_TO_GET_CONSTRUCTOR_ON_DESERIALIZATION, getDeclaringWeldClass(), signature);
        }
    }

    @Override
    public WeldConstructor<T> get() {
        return constructor;
    }
}
