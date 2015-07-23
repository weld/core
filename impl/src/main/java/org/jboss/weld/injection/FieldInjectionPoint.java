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

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.decorator.Delegate;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.jboss.weld.bean.proxy.DecoratorProxy;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.introspector.ForwardingWeldField;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.TypeVariableResolver;

import static org.jboss.weld.injection.Exceptions.rethrowException;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;

public class FieldInjectionPoint<T, X> extends ForwardingWeldField<T, X> implements WeldInjectionPoint<T, Field>, Serializable {

    @SuppressWarnings(value = "SE_BAD_FIELD", justification = "If the bean is not serializable, we won't ever try to serialize the injection point")
    private final Bean<?> declaringBean;
    private final WeldClass<?> injectionTargetClass;    // used only when declaringBean is null
    private final WeldField<T, X> field;
    private final boolean delegate;
    private final boolean cacheable;
    private final WeldManager beanManager;
    private Bean<?> cachedBean;
    private Type type;

    public static <T, X> FieldInjectionPoint<T, X> of(Bean<?> declaringBean, WeldClass<?> injectionTargetClass, WeldField<T, X> field, WeldManager beanManager) {
        return new FieldInjectionPoint<T, X>(declaringBean, injectionTargetClass, field, beanManager);
    }

    protected FieldInjectionPoint(Bean<?> declaringBean, WeldClass<?> injectionTargetClass, WeldField<T, X> field, WeldManager manager) {
        assert (declaringBean != null || injectionTargetClass != null) : "both declaringBean and injectionTargetClass are null";
        this.declaringBean = declaringBean;
        this.injectionTargetClass = injectionTargetClass;
        this.field = field;
        this.delegate = isAnnotationPresent(Inject.class) && isAnnotationPresent(Delegate.class) && declaringBean instanceof Decorator<?>;
        this.cacheable = !delegate && !InjectionPoint.class.isAssignableFrom(field.getJavaMember().getType()) && !Instance.class.isAssignableFrom(field.getJavaMember().getType());
        this.beanManager = manager;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldInjectionPoint<?, ?>) {
            FieldInjectionPoint<?, ?> ip = (FieldInjectionPoint<?, ?>) obj;
            if (AnnotatedTypes.compareAnnotatedField(field, ip.field)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    protected WeldField<T, X> delegate() {
        return field;
    }

    public Bean<?> getBean() {
        return declaringBean;
    }

    public WeldClass<?> getInjectionTargetClass() {
        return injectionTargetClass;
    }

    public WeldField<T, X> getWeldField() {
        return field;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return delegate().getQualifiers();
    }

    public void inject(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
        try {
            Object instanceToInject = declaringInstance;
            if (!(instanceToInject instanceof DecoratorProxy)) {
                // if declaringInstance is a proxy, unwrap it
                if (declaringInstance instanceof TargetInstanceProxy) {
                    instanceToInject = Reflections.<TargetInstanceProxy<T>>cast(declaringInstance).getTargetInstance();
                }
            }
            Object objectToInject;
            if (!cacheable) {
                objectToInject = manager.getInjectableReference(this, creationalContext);
            } else {
                if (cachedBean == null) {
                    cachedBean = manager.resolve(manager.getBeans(this));
                }
                objectToInject = manager.getReference(this, cachedBean, creationalContext);
            }
            delegate().set(instanceToInject, objectToInject);
        } catch (IllegalArgumentException e) {
            rethrowException(e);
        } catch (IllegalAccessException e) {
            rethrowException(e);
        }
    }

    public void inject(Object declaringInstance, Object value) {
        try {
            Object instanceToInject = declaringInstance;
            if (!(instanceToInject instanceof DecoratorProxy)) {
                // if declaringInstance is a proxy, unwrap it
                if (instanceToInject instanceof TargetInstanceProxy)
                    instanceToInject = Reflections.<TargetInstanceProxy<T>>cast(declaringInstance).getTargetInstance();
            }
            delegate().set(instanceToInject, value);
        } catch (IllegalArgumentException e) {
            rethrowException(e);
        } catch (IllegalAccessException e) {
            rethrowException(e);
        }
    }

    public Annotated getAnnotated() {
        return delegate();
    }

    public boolean isDelegate() {
        return delegate;
    }

    public Type getType() {
        if (type == null) {
            type = TypeVariableResolver.resolveVariables(getBean() == null ? getInjectionTargetClass().getJavaClass() : getBean().getBeanClass(), getBaseType());
        }
        return type;
    }

    public Member getMember() {
        return getJavaMember();
    }

    protected WeldManager getBeanManager() {
        return beanManager;
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy<T> extends WeldInjectionPointSerializationProxy<T, Field> {

        private static final long serialVersionUID = -3491482804822264969L;

        private final String fieldName;
        private final String injectionTargetClassName;

        public SerializationProxy(FieldInjectionPoint<T, ?> injectionPoint) {
            super(injectionPoint, injectionPoint.getBeanManager());
            this.fieldName = injectionPoint.getName();
            this.injectionTargetClassName = injectionPoint.getInjectionTargetClass().getName();
        }

        private Object readResolve() {
            WeldField<T, ?> field = getWeldField();
            if (field == null || (getDeclaringBean() == null && getDeclaringBeanId() != null)) {
                throw new IllegalStateException(ReflectionMessage.UNABLE_TO_GET_FIELD_ON_DESERIALIZATION, getDeclaringBeanId(), getDeclaringWeldClass(), fieldName);
            }

            return FieldInjectionPoint.of(getDeclaringBean(), getInjectionTargetClass(), getWeldField(), getBeanManager());
        }

        protected WeldClass<?> getInjectionTargetClass() {
            final Class<?> clazz = getService(ResourceLoader.class).classForName(injectionTargetClassName);
            return getService(ClassTransformer.class).loadClass(clazz);
        }

        protected WeldField<T, ?> getWeldField() {
            return getDeclaringWeldClass().getDeclaredWeldField(fieldName);
        }

    }


}
