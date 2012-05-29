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

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CIRCULAR_CALL;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_METHOD_CANNOT_HAVE_A_WILDCARD_RETURN_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_METHOD_WITH_TYPE_VARIABLE_RETURN_TYPE_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.RETURN_TYPE_MUST_BE_CONCRETE;
import static org.jboss.weld.logging.messages.BeanMessage.DECLARING_BEAN_MISSING;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * Common functionality for {@link Producer}s backing producer fields and producer methods.
 *
 * @author Jozef Hartinger
 */
public abstract class AbstractMemberProducer<X, T> extends AbstractProducer<T> {

    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private final DisposalMethod<?, ?> disposalMethod;
    private final CurrentInjectionPoint currentInjectionPointService;

    public AbstractMemberProducer(EnhancedAnnotatedMember<T, ? super X, ? extends Member> enhancedMember, DisposalMethod<?, ?> disposalMethod) {
        this.disposalMethod = disposalMethod;
        this.currentInjectionPointService = getBeanManager().getServices().get(CurrentInjectionPoint.class);
        checkDeclaringBean();
        checkProducerReturnType(enhancedMember);
    }

    protected void checkDeclaringBean() {
        if (getDeclaringBean() == null && !getAnnotated().isStatic()) {
            throw new org.jboss.weld.exceptions.IllegalArgumentException(DECLARING_BEAN_MISSING, getAnnotated());
        }
    }

    /**
     * Validates the producer method
     */
    protected void checkProducerReturnType(EnhancedAnnotatedMember<T, ? super X, ? extends Member> enhancedMember) {
        if ((enhancedMember.getBaseType() instanceof TypeVariable<?>) || (enhancedMember.getBaseType() instanceof WildcardType)) {
            throw new DefinitionException(RETURN_TYPE_MUST_BE_CONCRETE, enhancedMember.getBaseType());
        } else if (enhancedMember.isParameterizedType()) {
            boolean dependent = getBean() != null && Dependent.class.equals(getBean().getScope());
            for (Type type : enhancedMember.getActualTypeArguments()) {
                if (!dependent && type instanceof TypeVariable<?>) {
                    throw new DefinitionException(PRODUCER_METHOD_WITH_TYPE_VARIABLE_RETURN_TYPE_MUST_BE_DEPENDENT, enhancedMember);
                } else if (type instanceof WildcardType) {
                    throw new DefinitionException(PRODUCER_METHOD_CANNOT_HAVE_A_WILDCARD_RETURN_TYPE, enhancedMember);
                }
            }
        }
    }

    /**
     * Gets the receiver of the product. The two creational contexts need to be separated because the receiver only serves the product
     * creation (it is not a dependent instance of the created instance).
     *
     * @param productCreationalContext the creational context of the produced instance
     * @param receiverCreationalCOntext the creational context of the receiver
     * @return The receiver
     */
    protected Object getReceiver(CreationalContext<?> productCreationalContext, CreationalContext<?> receiverCreationalContext) {
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
                    log.warn(CIRCULAR_CALL, getAnnotated(), getDeclaringBean());
                    return incompleteInstance;
                }
            }
            return getBeanManager().getReference(getDeclaringBean(), receiverCreationalContext, true);
        }
    }

    /**
     * If metadata is required by the disposer method, store it within the CreationalContext.
     */
    private void storeMetadata(CreationalContext<T> creationalContext) {
        if (disposalMethod != null) {
            if (disposalMethod.hasBeanMetadataParameter()) {
                WeldCreationalContext<T> ctx = getWeldCreationalContext(creationalContext);
                checkValue(ctx.getContextual());
                ctx.storeContextual();
            }
            if (disposalMethod.hasInjectionPointMetadataParameter()) {
                InjectionPoint ip = currentInjectionPointService.peek();
                checkValue(ip);
                getWeldCreationalContext(creationalContext).storeInjectionPoint(ip);
            }
        }
    }

    private <A> WeldCreationalContext<A> getWeldCreationalContext(CreationalContext<A> ctx) {
        if (ctx instanceof WeldCreationalContext<?>) {
            return Reflections.cast(ctx);
        }
        throw new IllegalArgumentException("Unable to store values in " + ctx);
    }

    private void checkValue(Object object) {
        InjectionPoint ip = currentInjectionPointService.peek();
        if (ip != null && Beans.isPassivatingScope(ip.getBean(), getBeanManager()) && !(isTypeSerializable(object.getClass()))) {
            throw new IllegalArgumentException("Unable to store non-serializable " + object + " as a dependency of " + this);
        }
    }

    // according to the spec, anyone may call this method
    // we are not able to metadata since we do not have the CreationalContext of the producer bean
    // we create a new CreationalContext just for the invocation of the disposer method
    public void dispose(T instance) {
        CreationalContext<T> ctx = getBeanManager().createCreationalContext(null);
        try {
            dispose(instance, ctx);
        } finally {
            ctx.release();
        }
    }

    // invoke a disposer method - if exists
    // if the disposer metod requires bean metadata, it can be loaded from the CreationalContext
    public void dispose(T instance, CreationalContext<T> ctx) {
        if (disposalMethod != null) {
            if (disposalMethod.hasInjectionPointMetadataParameter()) {
                loadMetadataForDisposerInvocation(ctx);
            }
            try {
                Object receiver = getReceiver(ctx, ctx);
                disposalMethod.invokeDisposeMethod(receiver, instance, ctx);
            } finally {
                if (disposalMethod.hasInjectionPointMetadataParameter()) {
                    currentInjectionPointService.pop();
                }
            }
        }
    }

    protected void loadMetadataForDisposerInvocation(CreationalContext<T> ctx) {
        WeldCreationalContext<T> weldCtx = getWeldCreationalContext(ctx);
        InjectionPoint ip = weldCtx.loadInjectionPoint();
        if (ip == null) {
            throw new IllegalStateException("Unable to restore InjectionPoint instance.");
        }
        currentInjectionPointService.push(ip);
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        storeMetadata(ctx);
        CreationalContext<X> receiverCreationalContext = getBeanManager().createCreationalContext(getDeclaringBean());
        Object receiver = getReceiver(ctx, receiverCreationalContext);

        try {
            return produce(receiver, ctx);
        } finally {
            receiverCreationalContext.release();
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
        if (getDeclaringBean() == null) {
            return "Producer for " + getAnnotated();
        } else if (getBean() == null) {
            return "Producer for " + getAnnotated() + " declared on " + getDeclaringBean();
        } else {
            return "Producer for " + getBean() + " declared on " + getDeclaringBean();
        }
    }
}
