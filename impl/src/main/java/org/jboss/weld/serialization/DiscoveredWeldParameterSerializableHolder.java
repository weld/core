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

import org.jboss.weld.introspector.ConstructorSignature;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import static org.jboss.weld.logging.messages.BeanMessage.*;
import org.jboss.weld.logging.messages.ReflectionMessage;
import static org.jboss.weld.util.reflection.Reflections.cast;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Serializable holder for a parameter that has been discovered an thus can be recreated from the class definition.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class DiscoveredWeldParameterSerializableHolder<T, X> extends AbstractWeldAnnotatedHolder<X> implements SerializableHolder<WeldParameter<T, X>> {

    private static final long serialVersionUID = -2947624534504847812L;

    private final int position;
    private final MethodSignature methodSignature;
    private final ConstructorSignature constructorSignature;
    private transient WeldParameter<T, X> parameter;

    public DiscoveredWeldParameterSerializableHolder(WeldParameter<T, X> parameter) {
        super(parameter.getDeclaringType().getJavaClass());
        this.parameter = parameter;
        this.position = parameter.getPosition();
        if (parameter.getDeclaringWeldCallable() instanceof WeldMethod<?, ?>) {
            this.methodSignature = ((WeldMethod<?, ?>) parameter.getDeclaringWeldCallable()).getSignature();
            this.constructorSignature = null;
        } else if (parameter.getDeclaringWeldCallable() instanceof WeldConstructor<?>) {
            this.methodSignature = null;
            this.constructorSignature = ((WeldConstructor<?>) parameter.getDeclaringWeldCallable()).getSignature();
        } else {
            throw new IllegalStateException(IP_NOT_CONSTRUCTOR_OR_METHOD, parameter);
        }
    }

    @Override
    public WeldParameter<T, X> get() {
        return parameter;
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        is.defaultReadObject();
        parameter = getWeldParameter();
        if (parameter == null) {
            throw new IllegalStateException(ReflectionMessage.UNABLE_TO_GET_PARAMETER_ON_DESERIALIZATION, getDeclaringWeldClass(), methodSignature, position);
        }
    }

    protected WeldParameter<T, X> getWeldParameter() {
        if (methodSignature != null) {
            WeldMethod<?, ?> method = getDeclaringWeldClass().getDeclaredWeldMethod(methodSignature);
            if (method.getParameters().size() > position) {
                return cast(method.getWeldParameters().get(position));
            } else {
                throw new IllegalStateException(PARAM_NOT_IN_PARAM_LIST, position, method.getParameters());
            }
        } else if (constructorSignature != null) {
            WeldConstructor<?> constructor = getDeclaringWeldClass().getDeclaredWeldConstructor(constructorSignature);
            if (constructor.getParameters().size() > position) {
                return cast(constructor.getWeldParameters().get(position));
            } else {
                throw new IllegalStateException(PARAM_NOT_IN_PARAM_LIST, position, constructor.getParameters());
            }
        } else {
            throw new IllegalStateException(CANNOT_READ_OBJECT);
        }
    }
}
