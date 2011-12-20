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
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Serializable holder for a method that has been discovered an thus can be recreated from the class definition.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class DiscoveredWeldMethodSerializableHolder<T, X> extends AbstractWeldAnnotatedHolder<X> implements SerializableHolder<WeldMethod<T, X>> {

    public static <T, X> DiscoveredWeldMethodSerializableHolder<T, X> of(WeldMethod<T, X> method) {
        return new DiscoveredWeldMethodSerializableHolder<T, X>(method);
    }

    private static final long serialVersionUID = 1742767397227399280L;

    private final MethodSignature signature;
    private transient WeldMethod<T, X> method;

    private DiscoveredWeldMethodSerializableHolder(WeldMethod<T, X> method) {
        super(method.getDeclaringType().getJavaClass());
        this.method = method;
        this.signature = method.getSignature();
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        is.defaultReadObject();
        this.method = Reflections.cast(getDeclaringWeldClass().getDeclaredWeldMethod(signature));
        if (method == null) {
            throw new IllegalStateException(ReflectionMessage.UNABLE_TO_GET_METHOD_ON_DESERIALIZATION, getDeclaringWeldClass(), signature);
        }
    }

    @Override
    public WeldMethod<T, X> get() {
        return method;
    }
}
