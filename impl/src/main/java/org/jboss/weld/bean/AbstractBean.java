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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.inject.Named;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.SpecializationAndEnablementRegistry;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.TypeEqualitySpecializationUtils;
import org.jboss.weld.serialization.spi.BeanIdentifier;

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

    protected Class<T> type;

    private boolean preInitialized;
    private boolean proxyRequired;

    private Producer<T> producer;

    private boolean ignoreFinalMethods;

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
        BeanLogger.LOG.creatingBean(getType());
        if (getScope() != null) {
            proxyRequired = isNormalScoped();
        } else {
            proxyRequired = false;
        }
        BeanLogger.LOG.qualifiersUsed(getQualifiers(), this);
        BeanLogger.LOG.usingName(getName(), this);
        BeanLogger.LOG.usingScope(getScope(), this);
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
                if (previousSpecializedBeanName != null && name != null
                        && !previousSpecializedBeanName.equals(specializedBean.getName())) {
                    // there may be multiple beans specialized by this bean - make sure they all share the same name
                    throw BeanLogger.LOG.beansWithDifferentBeanNamesCannotBeSpecialized(previousSpecializedBeanName,
                            specializedBean.getName(), this);
                }
                previousSpecializedBeanName = name;
                if (isNameDefined && name != null) {
                    throw BeanLogger.LOG.nameNotAllowedOnSpecialization(getAnnotated(), specializedBean.getAnnotated());
                }

                // When a specializing bean extends the raw type of a generic superclass, types of the generic superclass are
                // added into types of the specializing bean because of assignability rules. However, ParameterizedTypes among
                // these types are NOT types of the specializing bean (that's the way java works)
                boolean rawInsteadOfGeneric = (this instanceof AbstractClassBean<?>
                        && specializedBean.getBeanClass().getTypeParameters().length > 0
                        && !(((AbstractClassBean<?>) this).getBeanClass().getGenericSuperclass() instanceof ParameterizedType));
                for (Type specializedType : specializedBean.getTypes()) {
                    if (rawInsteadOfGeneric && specializedType instanceof ParameterizedType) {
                        throw BeanLogger.LOG.specializingBeanMissingSpecializedType(this, specializedType, specializedBean);
                    }
                    boolean contains = getTypes().contains(specializedType);
                    if (!contains) {
                        for (Type specializingType : getTypes()) {
                            // In case 'type' is a ParameterizedType, two bean types equivalent in the CDI sense may not be
                            // equal in the java sense. Therefore we have to use our own equality util.
                            if (TypeEqualitySpecializationUtils.areTheSame(specializingType, specializedType)) {
                                contains = true;
                                break;
                            }
                        }
                    }
                    if (!contains) {
                        throw BeanLogger.LOG.specializingBeanMissingSpecializedType(this, specializedType, specializedBean);
                    }
                }
            }
        }
    }

    protected void postSpecialize() {
        // Override qualifiers and the bean name
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        for (Annotation qualifier : attributes().getQualifiers()) {
            // Don't include implicitjakarta.enterprise.inject.Default qualifier
            if (!qualifier.equals(Default.Literal.INSTANCE) || getAnnotated().isAnnotationPresent(Default.class)) {
                qualifiers.add(qualifier);
            }
        }
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

    public boolean isIgnoreFinalMethods() {
        return ignoreFinalMethods;
    }

    public void setIgnoreFinalMethods() {
        this.ignoreFinalMethods = true;
    }

}
