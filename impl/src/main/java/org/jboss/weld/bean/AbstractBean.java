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
import static org.jboss.weld.logging.messages.BeanMessage.BEANS_WITH_DIFFERENT_BEAN_NAMES_CANNOT_BE_SPECIALIZED;
import static org.jboss.weld.logging.messages.BeanMessage.CREATING_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.NAME_NOT_ALLOWED_ON_SPECIALIZATION;
import static org.jboss.weld.logging.messages.BeanMessage.QUALIFIERS_USED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MISSING_SPECIALIZED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.USING_NAME;
import static org.jboss.weld.logging.messages.BeanMessage.USING_SCOPE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Named;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.SpecializationAndEnablementRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.slf4j.cal10n.LocLogger;

/**
 * An abstract bean representation common for all beans
 *
 * @param <T> the type of bean
 * @param <S> the Class<?> of the bean type
 * @author Pete Muir
 * @author Ales Justin
 * @author Jozef Hartinger
 */
public abstract class AbstractBean<T, S> extends RIBean<T> {

    private static final LocLogger log = loggerFactory().getLogger(BEAN);
    protected Class<T> type;

    private boolean preInitialized;
    private boolean proxyRequired;

    private Producer<T> producer;

    /**
     * Constructor
     *
     * @param beanManager The Bean manager
     */
    public AbstractBean(BeanAttributes<T> attributes, BeanIdentifier identifier, BeanManagerImpl beanManager) {
        super(attributes, identifier, beanManager);
    }

    /**
     * Initializes specialization. This method is called before {@link ProcessBeanAttributes} is fired and also after the event
     * if the {@link BeanAttributes} have been altered.
     */
    @Override
    public void preInitialize() {
        synchronized (this) {
            if (isSpecializing() && !preInitialized) {
                preInitialized = true;
                preSpecialize();
                specialize();
                checkSpecialization();
                postSpecialize();
            }
        }
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        preInitialize();
        log.trace(CREATING_BEAN, getType());
        if (getScope() != null) {
            proxyRequired = isNormalScoped();
        } else {
            proxyRequired = false;
        }
        log.trace(QUALIFIERS_USED, getQualifiers(), this);
        log.trace(USING_NAME, getName(), this);
        log.trace(USING_SCOPE, getScope(), this);
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        checkType();
    }

    protected abstract void checkType();

    /**
     * Validates specialization if this bean specializes another bean.
     */
    public void checkSpecialization() {
        if (isSpecializing()) {
            boolean isNameDefined = getAnnotated().isAnnotationPresent(Named.class);
            String previousSpecializedBeanName = null;
            for (AbstractBean<?, ?> specializedBean : getSpecializedBeans()) {
                String name = specializedBean.getName();
                if (previousSpecializedBeanName != null && name != null && !previousSpecializedBeanName.equals(specializedBean.getName())) {
                    // there may be multiple beans specialized by this bean - make sure they all share the same name
                    throw new DefinitionException(BEANS_WITH_DIFFERENT_BEAN_NAMES_CANNOT_BE_SPECIALIZED, previousSpecializedBeanName, specializedBean.getName(), this);
                }
                previousSpecializedBeanName = name;
                if (isNameDefined && name != null) {
                    throw new DefinitionException(NAME_NOT_ALLOWED_ON_SPECIALIZATION, getAnnotated());
                }
                for (Type type : specializedBean.getTypes()) {
                    if (!getTypes().contains(type)) {
                        throw new DefinitionException(SPECIALIZING_BEAN_MISSING_SPECIALIZED_TYPE, this, type, specializedBean);
                    }
                }
            }
        }
    }

    protected void postSpecialize() {
        // override qualifiers
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.addAll(attributes().getQualifiers());
        // override name
        String name = attributes().getName();
        for (AbstractBean<?, ?> specializedBean : getSpecializedBeans()) {
            qualifiers.addAll(specializedBean.getQualifiers());
            if (specializedBean.getName() != null) {
                name = specializedBean.getName();
            }
        }
        setAttributes(new ImmutableBeanAttributes<T>(qualifiers, name, attributes()));
    }

    protected void preSpecialize() {

    }

    protected void specialize() {

    }

    /**
     * Returns the annotated item the bean represents
     *
     * @return The annotated item
     */
    public abstract Annotated getAnnotated();

    /**
     * Returns the weld-enhanced annotated item the bean represents. The item is only available during bootstrap.
     * The method throws {@link IllegalStateException} at runtime.
     *
     * @throws IllegalStateException when invoked at runtime
     * @return The annotated item
     */
    public abstract EnhancedAnnotated<T, S> getEnhancedAnnotated();

    protected Set<? extends AbstractBean<?, ?>> getSpecializedBeans() {
        return getBeanManager().getServices().get(SpecializationAndEnablementRegistry.class).resolveSpecializedBeans(this);
    }

    /**
     * Gets the type of the bean
     *
     * @return The type
     */
    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean isDependent() {
        return Dependent.class.equals(getScope());
    }

    public boolean isNormalScoped() {
        return getBeanManager().getServices().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
    }

    public boolean isSpecializing() {
        return getAnnotated().isAnnotationPresent(Specializes.class);
    }

    @Override
    public boolean isProxyRequired() {
        return proxyRequired;
    }

    public Producer<T> getProducer() {
        return producer;
    }

    /**
     * Set a Producer for this bean. This operation is *not* threadsafe, and should not be called outside bootstrap.
     */
    public void setProducer(Producer<T> producer) {
        this.producer = producer;
    }
}
