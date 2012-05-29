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

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.ERROR_DESTROYING;
import static org.jboss.weld.logging.messages.BeanMessage.PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL;
import static org.jboss.weld.logging.messages.BeanMessage.PUBLIC_FIELD_ON_NORMAL_SCOPED_BEAN_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Formats;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLogger.Level;

/**
 * Represents a simple bean
 *
 * @param <T> The type (class) of the bean
 * @author Pete Muir
 * @author Marius Bogoevici
 * @author Ales Justin
 */
public class ManagedBean<T> extends AbstractClassBean<T> {

    // Logger
    private static final LocLogger log = loggerFactory().getLogger(BEAN);
    private static final XLogger xLog = loggerFactory().getXLogger(BEAN);

    private ManagedBean<?> specializedBean;

    private final boolean proxiable;

    /**
     * Creates a simple, annotation defined Web Bean
     *
     * @param <T>         The type
     * @param clazz       The class
     * @param beanManager the current manager
     * @return A Web Bean
     */
    public static <T> ManagedBean<T> of(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> clazz, BeanManagerImpl beanManager) {
        if (clazz.isDiscovered()) {
            return new ManagedBean<T>(attributes, clazz, createSimpleId(ManagedBean.class.getSimpleName(), clazz), beanManager);
        } else {
            return new ManagedBean<T>(attributes, clazz, createId(ManagedBean.class.getSimpleName(), clazz), beanManager);
        }
    }

    protected static String createSimpleId(String beanType, EnhancedAnnotatedType<?> clazz) {
        return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(clazz.getBaseType()).toString();
    }

    /**
     * create a more complete id for types that have been added through the SPI
     * to prevent duplicate id's
     */
    protected static String createId(String beanType, EnhancedAnnotatedType<?> clazz) {
        return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(AnnotatedTypes.createTypeId(clazz)).toString();
    }

    /**
     * Constructor
     *
     * @param type        The type of the bean
     * @param beanManager The Bean manager
     */
    protected ManagedBean(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, String idSuffix, BeanManagerImpl beanManager) {
        super(attributes, type, idSuffix, beanManager);
        this.proxiable = Proxies.isTypesProxyable(type.getTypeClosure());
        setProducer(beanManager.createInjectionTarget(getEnhancedAnnotated(), this));
    }

    /**
     * Creates an instance of the bean
     *
     * @return The instance
     */
    public T create(CreationalContext<T> creationalContext) {
        T instance = getProducer().produce(creationalContext);
        getProducer().inject(instance, creationalContext);
        getProducer().postConstruct(instance);
        return instance;
    }

    /**
     * Destroys an instance of the bean
     *
     * @param instance The instance
     */
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        try {
            getProducer().preDestroy(instance);
            // WELD-1010 hack?
            if (creationalContext instanceof CreationalContextImpl) {
                ((CreationalContextImpl<T>) creationalContext).release(this, instance);
            } else {
                creationalContext.release();
            }
        } catch (Exception e) {
            log.error(ERROR_DESTROYING, this, instance);
            xLog.throwing(Level.DEBUG, e);
        }
    }

    /**
     * Validates the type
     */
    @Override
    protected void checkType() {
        if (!isDependent() && getEnhancedAnnotated().isParameterizedType()) {
            throw new DefinitionException(BEAN_MUST_BE_DEPENDENT, type);
        }
        boolean passivating = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(getScope()).isPassivating();
        if (passivating && !isPassivationCapableBean()) {
            throw new DeploymentException(PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL, this);
        }
    }

    @Override
    protected void checkBeanImplementation() {
        super.checkBeanImplementation();
        if (isNormalScoped()) {
            for (EnhancedAnnotatedField<?, ?> field : getEnhancedAnnotated().getEnhancedFields()) {
                if (field.isPublic() && !field.isStatic()) {
                    throw new DefinitionException(PUBLIC_FIELD_ON_NORMAL_SCOPED_BEAN_NOT_ALLOWED, field);
                }
            }
        }
    }

    @Override
    protected void preSpecialize() {
        super.preSpecialize();
        BeansClosure closure = BeansClosure.getClosure(beanManager);
        if (closure.isEJB(getEnhancedAnnotated().getEnhancedSuperclass())) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        }
    }

    @Override
    protected void specialize() {
        BeansClosure closure = BeansClosure.getClosure(beanManager);
        Bean<?> specializedBean = closure.getClassBean(getEnhancedAnnotated().getEnhancedSuperclass());
        if (specializedBean == null) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        }
        if (!(specializedBean instanceof ManagedBean<?>)) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        } else {
            this.specializedBean = (ManagedBean<?>) specializedBean;
        }
    }

    @Override
    public ManagedBean<?> getSpecializedBean() {
        return specializedBean;
    }

    @Override
    protected boolean isInterceptionCandidate() {
        return !((this instanceof InterceptorImpl<?>) || (this instanceof DecoratorImpl<?>));
    }

    @Override
    public String toString() {
        return "Managed Bean [" + getBeanClass().toString() + "] with qualifiers [" + Formats.formatAnnotations(getQualifiers()) + "]";
    }

    @Override
    public boolean isProxyable() {
        return proxiable;
    }
}
