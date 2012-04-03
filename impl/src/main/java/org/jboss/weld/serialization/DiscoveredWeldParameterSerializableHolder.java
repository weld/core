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

import static org.jboss.weld.logging.messages.BeanMessage.*;
import org.jboss.weld.logging.messages.ReflectionMessage;
import static org.jboss.weld.util.reflection.Reflections.cast;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Serializable holder for a parameter that has been discovered an thus can be recreated from the class definition.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class DiscoveredWeldParameterSerializableHolder<T, X> extends AbstractWeldAnnotatedHolder<X> implements SerializableHolder<EnhancedAnnotatedParameter<T, X>> {

    private static final long serialVersionUID = -2947624534504847812L;

    private final int position;
    private final MethodSignature methodSignature;
    private final ConstructorSignature constructorSignature;
    private transient EnhancedAnnotatedParameter<T, X> parameter;

    public DiscoveredWeldParameterSerializableHolder(EnhancedAnnotatedParameter<T, X> parameter) {
        super(parameter.getDeclaringType().getJavaClass());
        this.parameter = parameter;
        this.position = parameter.getPosition();
        if (parameter.getDeclaringEnhancedCallable() instanceof EnhancedAnnotatedMethod<?, ?>) {
            this.methodSignature = ((EnhancedAnnotatedMethod<?, ?>) parameter.getDeclaringEnhancedCallable()).getSignature();
            this.constructorSignature = null;
        } else if (parameter.getDeclaringEnhancedCallable() instanceof EnhancedAnnotatedConstructor<?>) {
            this.methodSignature = null;
            this.constructorSignature = ((EnhancedAnnotatedConstructor<?>) parameter.getDeclaringEnhancedCallable()).getSignature();
        } else {
            throw new IllegalStateException(IP_NOT_CONSTRUCTOR_OR_METHOD, parameter);
        }
    }

    @Override
    public EnhancedAnnotatedParameter<T, X> get() {
        return parameter;
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        is.defaultReadObject();
        parameter = getEnhancedParameter();
        if (parameter == null) {
            throw new IllegalStateException(ReflectionMessage.UNABLE_TO_GET_PARAMETER_ON_DESERIALIZATION, getDeclaringWeldClass(), methodSignature, position);
        }
    }

    protected EnhancedAnnotatedParameter<T, X> getEnhancedParameter() {
        if (methodSignature != null) {
            EnhancedAnnotatedMethod<?, ?> method = getDeclaringWeldClass().getDeclaredEnhancedMethod(methodSignature);
            if (method.getParameters().size() > position) {
                return cast(method.getEnhancedParameters().get(position));
            } else {
                throw new IllegalStateException(PARAM_NOT_IN_PARAM_LIST, position, method.getParameters());
            }
        } else if (constructorSignature != null) {
            EnhancedAnnotatedConstructor<?> constructor = getDeclaringWeldClass().getDeclaredEnhancedConstructor(constructorSignature);
            if (constructor.getParameters().size() > position) {
                return cast(constructor.getEnhancedParameters().get(position));
            } else {
                throw new IllegalStateException(PARAM_NOT_IN_PARAM_LIST, position, constructor.getParameters());
            }
        } else {
            throw new IllegalStateException(CANNOT_READ_OBJECT);
        }
    }
}
