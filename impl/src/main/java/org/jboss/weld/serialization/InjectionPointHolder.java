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

import java.io.Serializable;

import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.EmptyInjectionPoint;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.SerializationLogger;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Serializable holder for {@link InjectionPoint}.
 *
 * @author Jozef Hartinger
 *
 */
public class InjectionPointHolder extends AbstractSerializableHolder<InjectionPoint> {

    private static final long serialVersionUID = -6128821485743815308L;

    private final InjectionPointIdentifier identifier;

    public InjectionPointHolder(String contextId, InjectionPoint ip) {
        super(ip);
        Preconditions.checkNotNull(ip);
        if (ip.getBean() == null) {
            if (ip instanceof Serializable) {
                this.identifier = new SerializableInjectionPointIdentifier(ip);
            } else {
                this.identifier = new TransientInjectionPointIdentifier(ip);
            }
        } else if (ip.getAnnotated() instanceof AnnotatedField<?>) {
            AnnotatedField<?> field = Reflections.cast(ip.getAnnotated());
            this.identifier = new FieldInjectionPointIdentifier(contextId, ip.getBean(), field);
        } else if (ip.getAnnotated() instanceof AnnotatedParameter<?>) {
            AnnotatedParameter<?> parameter = Reflections.cast(ip.getAnnotated());
            if (parameter.getDeclaringCallable() instanceof AnnotatedConstructor<?>) {
                AnnotatedConstructor<?> constructor = Reflections.cast(parameter.getDeclaringCallable());
                this.identifier = new ConstructorParameterInjectionPointIdentifier(contextId, ip.getBean(),
                        parameter.getPosition(), constructor);
            } else if (parameter.getDeclaringCallable() instanceof AnnotatedMethod<?>) {
                AnnotatedMethod<?> method = Reflections.cast(parameter.getDeclaringCallable());
                this.identifier = new MethodParameterInjectionPointIdentifier(contextId, ip.getBean(), parameter.getPosition(),
                        method);
            } else {
                throw BeanLogger.LOG.invalidAnnotatedCallable(parameter.getDeclaringCallable());
            }
        } else {
            throw BeanLogger.LOG.invalidAnnotatedOfInjectionPoint(ip.getAnnotated(), ip);
        }
    }

    @Override
    protected InjectionPoint initialize() {
        final InjectionPoint ip = identifier.restoreInjectionPoint();
        if (ip == null) {
            SerializationLogger.LOG.debug("Unable to deserialize InjectionPoint metadata. Falling back to EmptyInjectionPoint");
            return EmptyInjectionPoint.INSTANCE;
        }
        return ip;
    }

    private interface InjectionPointIdentifier extends Serializable {
        InjectionPoint restoreInjectionPoint();
    }

    /**
     * Transient implementation of {@link InjectionPointIdentifier}. Holds an InjectionPint reference until serialized. After
     * deserialization the reference is lost.
     *
     * @author Jozef Hartinger
     *
     */
    private static class TransientInjectionPointIdentifier implements InjectionPointIdentifier {

        private static final long serialVersionUID = 6952579330771485841L;

        @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
        private final transient InjectionPoint ip;

        public TransientInjectionPointIdentifier(InjectionPoint ip) {
            this.ip = ip;
        }

        @Override
        public InjectionPoint restoreInjectionPoint() {
            return ip;
        }

    }

    /**
     * Holds a direct reference to an InjectionPoint provided it is {@link Serializable}
     *
     * @author Jozef Hartinger
     *
     */
    private static class SerializableInjectionPointIdentifier implements InjectionPointIdentifier {

        private static final long serialVersionUID = 6952579330771485841L;

        private final InjectionPoint ip;

        public SerializableInjectionPointIdentifier(InjectionPoint ip) {
            this.ip = ip;
        }

        @Override
        public InjectionPoint restoreInjectionPoint() {
            return ip;
        }

    }

    private abstract static class AbstractInjectionPointIdentifier implements InjectionPointIdentifier {

        private static final long serialVersionUID = -8167922066673252787L;

        private final BeanHolder<?> bean;

        public AbstractInjectionPointIdentifier(String contextId, Bean<?> bean) {
            this.bean = BeanHolder.of(contextId, bean);
        }

        @Override
        public InjectionPoint restoreInjectionPoint() {
            InjectionPoint injectionPoint = null;
            for (InjectionPoint ip : bean.get().getInjectionPoints()) {
                if (matches(ip)) {
                    if (injectionPoint != null) {
                        throw BeanLogger.LOG.unableToRestoreInjectionPointMultiple(bean.get(), injectionPoint, ip);
                    }
                    injectionPoint = ip;
                }
            }
            if (injectionPoint == null) {
                throw BeanLogger.LOG.unableToRestoreInjectionPoint(bean.get());
            }
            return injectionPoint;
        }

        protected abstract boolean matches(InjectionPoint ip);
    }

    private static class FieldInjectionPointIdentifier extends AbstractInjectionPointIdentifier {

        private static final long serialVersionUID = 4581216810217284043L;

        private final FieldHolder field;

        public FieldInjectionPointIdentifier(String contextId, Bean<?> bean, AnnotatedField<?> field) {
            super(contextId, bean);
            this.field = new FieldHolder(field.getJavaMember());
        }

        @Override
        protected boolean matches(InjectionPoint ip) {
            if (ip.getAnnotated() instanceof AnnotatedField<?>) {
                AnnotatedField<?> annotatedField = Reflections.cast(ip.getAnnotated());
                return (field.get().equals(annotatedField.getJavaMember()));
            }
            return false;
        }
    }

    private abstract static class AbstractParameterInjectionPointIdentifier extends AbstractInjectionPointIdentifier {

        private static final long serialVersionUID = -3618042716814281161L;

        private final int position;

        public AbstractParameterInjectionPointIdentifier(String contextId, Bean<?> bean, int position) {
            super(contextId, bean);
            this.position = position;
        }

        @Override
        protected boolean matches(InjectionPoint ip) {
            if (ip.getAnnotated() instanceof AnnotatedParameter<?>) {
                AnnotatedParameter<?> annotatedParameter = Reflections.cast(ip.getAnnotated());
                return position == annotatedParameter.getPosition() && matches(ip, annotatedParameter.getDeclaringCallable());
            }
            return false;
        }

        protected abstract boolean matches(InjectionPoint ip, AnnotatedCallable<?> annotatedCallable);
    }

    private static class ConstructorParameterInjectionPointIdentifier extends AbstractParameterInjectionPointIdentifier {

        private static final long serialVersionUID = 638702977751948835L;

        private final ConstructorHolder<?> constructor;

        public ConstructorParameterInjectionPointIdentifier(String contextId, Bean<?> bean, int position,
                AnnotatedConstructor<?> constructor) {
            super(contextId, bean, position);
            this.constructor = ConstructorHolder.of(constructor.getJavaMember());
        }

        @Override
        protected boolean matches(InjectionPoint ip, AnnotatedCallable<?> annotatedCallable) {
            if (annotatedCallable instanceof AnnotatedConstructor<?>) {
                AnnotatedConstructor<?> annotatedConstructor = Reflections.cast(annotatedCallable);
                return constructor.get().equals(annotatedConstructor.getJavaMember());
            }
            return false;
        }
    }

    private static class MethodParameterInjectionPointIdentifier extends AbstractParameterInjectionPointIdentifier {

        private static final long serialVersionUID = -3263543692438746424L;

        private final MethodHolder method;

        public MethodParameterInjectionPointIdentifier(String contextId, Bean<?> bean, int position,
                AnnotatedMethod<?> constructor) {
            super(contextId, bean, position);
            this.method = MethodHolder.of(constructor);
        }

        @Override
        protected boolean matches(InjectionPoint ip, AnnotatedCallable<?> annotatedCallable) {
            if (annotatedCallable instanceof AnnotatedMethod<?>) {
                AnnotatedMethod<?> annotatedMethod = Reflections.cast(annotatedCallable);
                return method.get().equals(annotatedMethod.getJavaMember());
            }
            return false;
        }
    }
}
