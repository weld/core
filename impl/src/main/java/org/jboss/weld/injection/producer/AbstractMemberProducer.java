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

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Producer;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.bean.ContextualInstance;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Common functionality for {@link Producer}s backing producer fields and producer methods.
 *
 * @author Jozef Hartinger
 * @author Marko Luksa
 */
public abstract class AbstractMemberProducer<X, T> extends AbstractProducer<T> {

    private final DisposalMethod<?, ?> disposalMethod;

    public AbstractMemberProducer(EnhancedAnnotatedMember<T, ? super X, ? extends Member> enhancedMember,
            DisposalMethod<?, ?> disposalMethod) {
        this.disposalMethod = disposalMethod;
        checkDeclaringBean();
        checkProducerReturnType(enhancedMember);
    }

    protected void checkDeclaringBean() {
        if (getDeclaringBean() == null && !getAnnotated().isStatic()) {
            throw BeanLogger.LOG.declaringBeanMissing(getAnnotated());
        }
    }

    protected void checkProducerReturnType(EnhancedAnnotatedMember<T, ? super X, ? extends Member> enhancedMember) {
        checkReturnTypeForWildcardsAndTypeVariables(enhancedMember, enhancedMember.getBaseType(), false);
    }

    private void checkReturnTypeForWildcardsAndTypeVariables(
            EnhancedAnnotatedMember<T, ? super X, ? extends Member> enhancedMember, Type type,
            boolean isParameterizedType) {
        if (type instanceof TypeVariable<?>) {
            if (isParameterizedType) {
                if (!isDependent()) {
                    throw producerWithParameterizedTypeWithTypeVariableBeanTypeMustBeDependent(enhancedMember);
                }
            } else {
                throw producerWithInvalidTypeVariable(enhancedMember);
            }
        } else if (type instanceof WildcardType) {
            throw producerWithInvalidWildcard(enhancedMember);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type parameterType : parameterizedType.getActualTypeArguments()) {
                checkReturnTypeForWildcardsAndTypeVariables(enhancedMember, parameterType, true);
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            checkReturnTypeForWildcardsAndTypeVariables(enhancedMember, arrayType.getGenericComponentType(), false);
        }
    }

    protected abstract DefinitionException producerWithInvalidTypeVariable(AnnotatedMember<?> member);

    protected abstract DefinitionException producerWithInvalidWildcard(AnnotatedMember<?> member);

    protected abstract DefinitionException producerWithParameterizedTypeWithTypeVariableBeanTypeMustBeDependent(
            AnnotatedMember<?> member);

    private boolean isDependent() {
        return getBean() != null && Dependent.class.equals(getBean().getScope());
    }

    /**
     * Gets the receiver of the product. The two creational contexts need to be separated because the receiver only serves the
     * product
     * creation (it is not a dependent instance of the created instance).
     *
     * @param productCreationalContext the creational context of the produced instance
     * @param receiverCreationalContext the creational context of the receiver
     * @return The receiver
     */
    protected Object getReceiver(CreationalContext<?> productCreationalContext,
            CreationalContext<?> receiverCreationalContext) {
        // This is a bit dangerous, as it means that producer methods can end up
        // executing on partially constructed instances. Also, it's not required
        // by the spec...
        if (getAnnotated().isStatic()) {
            return null;
        } else {
            if (productCreationalContext instanceof WeldCreationalContext<?>) {
                WeldCreationalContext<?> creationalContextImpl = (WeldCreationalContext<?>) productCreationalContext;
                final Object incompleteInstance = creationalContextImpl.getIncompleteInstance(getDeclaringBean());
                if (incompleteInstance != null) {
                    BeanLogger.LOG.circularCall(getAnnotated(), getDeclaringBean());
                    return incompleteInstance;
                }
            }
            return getBeanManager().getReference(getDeclaringBean(), null, receiverCreationalContext, true);
        }
    }

    public void dispose(T instance) {
        if (disposalMethod != null) {
            // CreationalContext is only created if we need it to obtain the receiver
            // MethodInvocationStrategy takes care of creating CC for parameters, if needed
            if (disposalMethod.getAnnotated().isStatic()) {
                disposalMethod.invokeDisposeMethod(null, instance, null);
            } else {
                WeldCreationalContext<X> ctx = null;
                try {
                    Object receiver = ContextualInstance.getIfExists(getDeclaringBean(), getBeanManager());
                    if (receiver == null) {
                        ctx = getBeanManager().createCreationalContext(null);
                        // Create child CC so that a dependent reciever may be destroyed after the disposer method completes
                        receiver = ContextualInstance.get(getDeclaringBean(), getBeanManager(),
                                ctx.getCreationalContext(getDeclaringBean()));
                    }
                    if (receiver != null) {
                        disposalMethod.invokeDisposeMethod(receiver, instance, ctx);
                    }
                } finally {
                    if (ctx != null) {
                        ctx.release();
                    }
                }
            }
        }
    }

    @Override
    public T produce(CreationalContext<T> ctx) {

        CreationalContext<X> receiverCreationalContext = getReceiverCreationalContext(ctx);
        Object receiver = getReceiver(ctx, receiverCreationalContext);

        try {
            return produce(receiver, ctx);
        } finally {
            receiverCreationalContext.release();
        }
    }

    private CreationalContext<X> getReceiverCreationalContext(CreationalContext<T> ctx) {
        if (ctx instanceof WeldCreationalContext) {
            return ((WeldCreationalContext<?>) ctx).getProducerReceiverCreationalContext(getDeclaringBean());
        } else {
            return getBeanManager().createCreationalContext(getDeclaringBean());
        }
    }

    public DisposalMethod<?, ?> getDisposalMethod() {
        return disposalMethod;
    }

    protected boolean isTypeSerializable(Object object) {
        return object instanceof Serializable;
    }

    public abstract BeanManagerImpl getBeanManager();

    public abstract Bean<X> getDeclaringBean();

    public abstract Bean<T> getBean();

    public abstract AnnotatedMember<? super X> getAnnotated();

    protected abstract T produce(Object receiver, CreationalContext<T> ctx);

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Producer for ");
        if (getDeclaringBean() == null) {
            result.append(getAnnotated());
        } else {
            if (getBean() == null) {
                result.append(getAnnotated());
            } else {
                result.append(getBean());
            }
            result.append(" declared on ").append(getDeclaringBean());
        }
        return result.toString();
    }
}
