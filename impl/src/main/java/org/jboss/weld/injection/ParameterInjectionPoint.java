/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.introspector.ConstructorSignature;
import org.jboss.weld.introspector.ForwardingWeldParameter;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

import javax.decorator.Delegate;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import static org.jboss.weld.logging.messages.BeanMessage.CANNOT_READ_OBJECT;
import static org.jboss.weld.logging.messages.BeanMessage.IP_NOT_CONSTRUCTOR_OR_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.PARAM_NOT_IN_PARAM_LIST;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.reflection.Reflections.cast;

public class ParameterInjectionPoint<T, X> extends ForwardingWeldParameter<T, X> implements WeldInjectionPoint<T, Object>, Serializable {

    public static <T, X> ParameterInjectionPoint<T, X> of(String contextId, Bean<?> declaringBean, WeldParameter<T, X> parameter) {
        return new ParameterInjectionPoint<T, X>(contextId, declaringBean, parameter);
    }

    @SuppressWarnings(value = "SE_BAD_FIELD", justification = "If the bean is not serializable, we won't ever try to serialize the injection point")
    private final Bean<?> declaringBean;
    private final WeldParameter<T, X> parameter;
    private final boolean delegate;
    private final boolean cacheable;
    private Bean<?> cachedBean;
    private String contextId;

    private ParameterInjectionPoint(String contextId, Bean<?> declaringBean, WeldParameter<T, X> parameter) {
        this.declaringBean = declaringBean;
        this.parameter = parameter;
        this.contextId = contextId;
        this.delegate = isAnnotationPresent(Delegate.class) && declaringBean instanceof Decorator<?>;
        this.cacheable = !delegate && !InjectionPoint.class.isAssignableFrom(parameter.getJavaClass()) && !Instance.class.isAssignableFrom(parameter.getJavaClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterInjectionPoint<?, ?>) {
            ParameterInjectionPoint<?, ?> ip = (ParameterInjectionPoint<?, ?>) obj;
            if (parameter.getDeclaringWeldCallable().getJavaMember().equals(ip.parameter.getDeclaringWeldCallable().getJavaMember()) && parameter.getAnnotations().equals(ip.parameter.getAnnotations()) && parameter.getPosition() == ip.parameter.getPosition()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return parameter.hashCode();
    }

    @Override
    protected WeldParameter<T, X> delegate() {
        return parameter;
    }

    public Bean<?> getBean() {
        return declaringBean;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return delegate().getQualifiers();
    }

    public Member getJavaMember() {
        return delegate().getDeclaringCallable().getJavaMember();
    }

    public void inject(Object declaringInstance, Object value) {
        throw new UnsupportedOperationException();
    }

    public T getValueToInject(BeanManagerImpl manager, CreationalContext<?> creationalContext) {
        T objectToInject;
        if (!cacheable) {
            objectToInject = Reflections.<T>cast(manager.getInjectableReference(this, creationalContext));
        } else {
            if (cachedBean == null) {
                cachedBean = manager.resolve(manager.getBeans(this));
            }
            objectToInject = Reflections.<T>cast(manager.getReference(this, cachedBean, creationalContext));
        }
        return objectToInject;
    }

    public Annotated getAnnotated() {
        return delegate();
    }

    public boolean isDelegate() {
        return delegate;
    }

    public boolean isTransient() {
        // TODO Auto-generated method stub
        return false;
    }

    public Type getType() {
        return getBaseType();
    }

    public Member getMember() {
        return getJavaMember();
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(contextId, this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy<T> extends WeldInjectionPointSerializationProxy<T, Object> {

        private static final long serialVersionUID = -3491482804822264969L;

        private final int parameterPosition;
        private final MethodSignature methodSignature;
        private final ConstructorSignature constructorSignature;

        public SerializationProxy(String contextId, ParameterInjectionPoint<T, ?> injectionPoint) {
            super(contextId, injectionPoint);
            this.parameterPosition = injectionPoint.getPosition();
            if (injectionPoint.delegate().getDeclaringWeldCallable() instanceof WeldMethod<?, ?>) {
                this.methodSignature = ((WeldMethod<?, ?>) injectionPoint.delegate().getDeclaringWeldCallable()).getSignature();
                this.constructorSignature = null;
            } else if (injectionPoint.delegate().getDeclaringWeldCallable() instanceof WeldConstructor<?>) {
                this.methodSignature = null;
                this.constructorSignature = ((WeldConstructor<?>) injectionPoint.delegate().getDeclaringWeldCallable()).getSignature();
            } else {
                throw new IllegalStateException(IP_NOT_CONSTRUCTOR_OR_METHOD, injectionPoint);
            }
        }

        private Object readResolve() {
            WeldParameter<T, ?> parameter = getWeldParameter();
            Bean<T> bean = getDeclaringBean();
            if (parameter == null || (bean == null && getDeclaringBeanId() != null)) {
                throw new IllegalStateException(ReflectionMessage.UNABLE_TO_GET_PARAMETER_ON_DESERIALIZATION, getDeclaringBeanId(), getDeclaringWeldClass(), methodSignature, parameterPosition);
            }
            return ParameterInjectionPoint.of(contextId, getDeclaringBean(), getWeldParameter());
        }

        protected WeldParameter<T, ?> getWeldParameter() {
            if (methodSignature != null) {
                WeldMethod<?, ?> method = getDeclaringWeldClass().getDeclaredWeldMethod(methodSignature);
                if (method.getParameters().size() > parameterPosition) {
                    return cast(method.getWeldParameters().get(parameterPosition));
                } else {
                    throw new IllegalStateException(PARAM_NOT_IN_PARAM_LIST, parameterPosition, method.getParameters());
                }
            } else if (constructorSignature != null) {
                WeldConstructor<?> constructor = getDeclaringWeldClass().getDeclaredWeldConstructor(constructorSignature);
                if (constructor.getParameters().size() > parameterPosition) {
                    return cast(constructor.getWeldParameters().get(parameterPosition));
                } else {
                    throw new IllegalStateException(PARAM_NOT_IN_PARAM_LIST, parameterPosition, constructor.getParameters());
                }
            } else {
                throw new IllegalStateException(CANNOT_READ_OBJECT);
            }

        }

    }


}
