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

import static org.jboss.weld.logging.messages.BeanMessage.INCONSISTENT_ANNOTATIONS_ON_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.METHOD_NOT_BUSINESS_METHOD;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * {@link Producer} implementation for producer methods.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class ProducerMethodProducer<X, T> extends AbstractMemberProducer<X, T> {

    // The underlying method
    private final MethodInjectionPoint<T, ? super X> method;

    public ProducerMethodProducer(EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod, DisposalMethod<?, ?> disposalMethod) {
        super(enhancedAnnotatedMethod, disposalMethod);
        this.method = InjectionPointFactory.instance().createMethodInjectionPoint(enhancedAnnotatedMethod, getBean(), enhancedAnnotatedMethod.getDeclaringType().getJavaClass(), false, getBeanManager());
        checkProducerMethod(enhancedAnnotatedMethod);
        checkDelegateInjectionPoints();
    }

    /**
     * Validates the producer method
     */
    protected void checkProducerMethod(EnhancedAnnotatedMethod<T, ? super X> method) {
        if (method.getEnhancedParameters(Observes.class).size() > 0) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Observes");
        } else if (method.getEnhancedParameters(Disposes.class).size() > 0) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Disposes");
        } else if (getDeclaringBean() instanceof SessionBean<?>) {
            boolean methodDeclaredOnTypes = false;
            // TODO use annotated item?
            for (Type type : getDeclaringBean().getTypes()) {
                if (type instanceof Class<?>) {
                    if (SecureReflections.isMethodExists((Class<?>) type, method.getName(), method.getParameterTypesAsArray())) {
                        methodDeclaredOnTypes = true;
                        continue;
                    }
                }
            }
            if (!methodDeclaredOnTypes) {
                throw new DefinitionException(METHOD_NOT_BUSINESS_METHOD, this, getDeclaringBean());
            }
        }
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return method.getInjectionPoints();
    }

    @Override
    protected T produce(Object receiver, CreationalContext<T> ctx) {
        if (receiver != null) {
            return method.invokeOnInstance(receiver, getBeanManager(), ctx, CreationException.class);
        } else {
            return method.invoke(null, getBeanManager(), ctx, CreationException.class);
        }
    }

    @Override
    public AnnotatedMember<? super X> getAnnotated() {
        return method.getAnnotated();
    }
}
