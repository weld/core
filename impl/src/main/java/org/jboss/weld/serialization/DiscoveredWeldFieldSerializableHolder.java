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
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Serializable holder for a field that has been discovered an thus can be recreated from the class definition.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class DiscoveredWeldFieldSerializableHolder<T, X> extends AbstractWeldAnnotatedHolder<X> implements SerializableHolder<WeldField<T, X>> {

    private static final long serialVersionUID = -5186616992990718551L;

    private final String name;
    private transient WeldField<T, X> field;

    public DiscoveredWeldFieldSerializableHolder(WeldField<T, X> field) {
        super(field.getDeclaringType().getJavaClass());
        this.field = field;
        this.name = field.getName();
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        is.defaultReadObject();
        field = Reflections.cast(getDeclaringWeldClass().getDeclaredWeldField(name));
        if (field == null) {
            throw new IllegalStateException(ReflectionMessage.UNABLE_TO_GET_FIELD_ON_DESERIALIZATION, getDeclaringWeldClass(), name);
        }
    }

    @Override
    public WeldField<T, X> get() {
        return field;
    }
}