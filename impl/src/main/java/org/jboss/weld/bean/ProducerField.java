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
package org.jboss.weld.bean;

import java.lang.reflect.Field;

import javax.enterprise.context.spi.CreationalContext;
import javax.inject.Inject;

import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import static org.jboss.weld.logging.messages.BeanMessage.INJECTED_FIELD_CANNOT_BE_PRODUCER;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_FIELD_ON_SESSION_BEAN_MUST_BE_STATIC;

/**
 * Represents a producer field
 *
 * @param <T>
 * @author Pete Muir
 */
public class ProducerField<X, T> extends AbstractProducerBean<X, T, Field> {

    // The underlying field
    private WeldField<T, ? super X> field;
    private final boolean proxiable;

    /**
     * Creates a producer field
     *
     * @param field         The underlying method abstraction
     * @param declaringBean The declaring bean abstraction
     * @param beanManager   the current manager
     * @return A producer field
     */
    public static <X, T> ProducerField<X, T> of(WeldField<T, ? super X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl beanManager, ServiceRegistry services) {
        return new ProducerField<X, T>(field, declaringBean, beanManager, services);
    }


    /**
     * Constructor
     *
     * @param field        The producer field abstraction
     * @param declaringBean The declaring bean
     * @param manager       The Bean manager
     * @param services      The services
     */
    protected ProducerField(WeldField<T, ? super X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl manager, ServiceRegistry services) {
        super(createId(field, declaringBean), declaringBean, manager, services);
        this.field = field;
        initType();
        initTypes();
        initQualifiers();
        initStereotypes();
        this.proxiable = Proxies.isTypesProxyable(field.getTypeClosure(), manager.getContextId());
    }

    protected static String createId(WeldField<?, ?> field, AbstractClassBean<?> declaringBean) {
        if (declaringBean.getWeldAnnotated().isDiscovered()) {
            StringBuilder sb = new StringBuilder();
            sb.append(ProducerField.class.getSimpleName());
            sb.append(BEAN_ID_SEPARATOR);
            sb.append(declaringBean.getWeldAnnotated().getName());
            sb.append(".");
            sb.append(field.getName());
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(ProducerField.class.getSimpleName());
            sb.append(BEAN_ID_SEPARATOR);
            sb.append(AnnotatedTypes.createTypeId(declaringBean.getWeldAnnotated()));
            sb.append(".");
            sb.append(AnnotatedTypes.createFieldId(field));
            return sb.toString();
        }
    }

    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        if (!isInitialized()) {
            super.initialize(environment);
            setProducer(new ProducerFieldProducer());
            checkProducerField();
        }
    }

    private class ProducerFieldProducer extends AbstractProducer {

        public void dispose(T instance) {
            defaultDispose(instance);
        }

        public T produce(Object receiver, CreationalContext<T> creationalContext) {
            // unwrap if we have a proxy
            if (receiver instanceof TargetInstanceProxy) {
                receiver = Reflections.<TargetInstanceProxy<T>>cast(receiver).getTargetInstance();
            }
            return field.get(receiver);
        }

        @Override
        public String toString() {
            return field.toString();
        }
    }


    protected void checkProducerField() {
        if (getWeldAnnotated().isAnnotationPresent(Inject.class)) {
            throw new DefinitionException(INJECTED_FIELD_CANNOT_BE_PRODUCER, getWeldAnnotated(), getWeldAnnotated().getDeclaringType());
        }
        if (getDeclaringBean() instanceof SessionBean<?> && !field.isStatic()) {
            throw new DefinitionException(PRODUCER_FIELD_ON_SESSION_BEAN_MUST_BE_STATIC, getWeldAnnotated(), getWeldAnnotated().getDeclaringType());
        }
    }

    protected void defaultDispose(T instance) {
        // No disposal by default
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        getProducer().dispose(instance);
    }

    /**
     * Gets the annotated item representing the field
     *
     * @return The annotated item
     */
    @Override
    public WeldField<T, ? super X> getWeldAnnotated() {
        return field;
    }

    /**
     * Returns the default name
     *
     * @return The default name
     */
    @Override
    protected String getDefaultName() {
        return field.getPropertyName();
    }

    @Override
    public AbstractBean<?, ?> getSpecializedBean() {
        return null;
    }

    @Override
    public boolean isSpecializing() {
        return false;
    }

    @Override
    public String toString() {
        return "Producer Field [" + Formats.formatType(getWeldAnnotated().getBaseType()) + "] with qualifiers [" + Formats.formatAnnotations(getQualifiers()) + "] declared as [" + getWeldAnnotated() + "]";
    }

    @Override
    public boolean isProxyable() {
        return proxiable;
    }


    @Override
    public boolean hasDefaultProducer() {
        return getProducer() instanceof ProducerField.ProducerFieldProducer;
    }

}
