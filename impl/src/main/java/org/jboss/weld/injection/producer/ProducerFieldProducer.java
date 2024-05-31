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
package org.jboss.weld.injection.producer;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.inject.Inject;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * {@link Producer} implementation for producer fields.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class ProducerFieldProducer<X, T> extends AbstractMemberProducer<X, T> {

    private final Field accessibleField;

    public ProducerFieldProducer(EnhancedAnnotatedField<T, ? super X> enhancedField, DisposalMethod<?, ?> disposalMethod) {
        super(enhancedField, disposalMethod);
        this.accessibleField = Reflections.getAccessibleCopyOfMember(enhancedField.getJavaMember());
        checkProducerField(enhancedField);
    }

    protected void checkProducerField(EnhancedAnnotatedField<T, ? super X> enhancedField) {
        if (getDeclaringBean() instanceof SessionBean<?> && !enhancedField.isStatic()) {
            throw BeanLogger.LOG.producerFieldOnSessionBeanMustBeStatic(enhancedField, enhancedField.getDeclaringType());
        }
        if (enhancedField.isAnnotationPresent(Inject.class)) {
            if (getDeclaringBean() != null) {
                throw BeanLogger.LOG.injectedFieldCannotBeProducer(enhancedField, getDeclaringBean());
            } else {
                throw BeanLogger.LOG.injectedFieldCannotBeProducer(enhancedField, enhancedField.getDeclaringType());
            }
        }
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public abstract AnnotatedField<? super X> getAnnotated();

    @Override
    public T produce(Object receiver, CreationalContext<T> creationalContext) {
        // unwrap if we have a proxy
        if (receiver instanceof TargetInstanceProxy) {
            receiver = Reflections.<TargetInstanceProxy<T>> cast(receiver).weld_getTargetInstance();
        }
        try {
            return cast(accessibleField.get(receiver));
        } catch (IllegalAccessException e) {
            throw UtilLogger.LOG.accessErrorOnField(accessibleField.getName(), accessibleField.getDeclaringClass(), e);
        }
    }

    @Override
    public String toString() {
        return getAnnotated().toString();
    }

    @Override
    protected DefinitionException producerWithInvalidTypeVariable(AnnotatedMember<?> member) {
        return BeanLogger.LOG.producerFieldTypeInvalidTypeVariable(member,
                Formats.formatAsStackTraceElement(member.getJavaMember()));
    }

    @Override
    protected DefinitionException producerWithInvalidWildcard(AnnotatedMember<?> member) {
        return BeanLogger.LOG.producerFieldCannotHaveAWildcardBeanType(member,
                Formats.formatAsStackTraceElement(member.getJavaMember()));
    }

    @Override
    protected DefinitionException producerWithParameterizedTypeWithTypeVariableBeanTypeMustBeDependent(
            AnnotatedMember<?> member) {
        return BeanLogger.LOG.producerFieldWithTypeVariableBeanTypeMustBeDependent(member,
                Formats.formatAsStackTraceElement(member.getJavaMember()));
    }
}
