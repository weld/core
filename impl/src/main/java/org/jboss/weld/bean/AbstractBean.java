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
import static org.jboss.weld.logging.messages.BeanMessage.*;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CREATING_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_NOT_ON_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.NAME_NOT_ALLOWED_ON_SPECIALIZATION;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MISSING_SPECIALIZED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.TYPED_CLASS_NOT_IN_HIERARCHY;
import static org.jboss.weld.util.collections.WeldCollections.immutableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.inject.Named;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.collections.ArraySet;
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

    private Set<WeldInjectionPoint<?, ?>> injectionPoints;
    private Set<WeldInjectionPoint<?, ?>> delegateInjectionPoints;
    private Set<WeldInjectionPoint<?, ?>> newInjectionPoints;
    private final ServiceRegistry services;
    private boolean preInitialized;
    private boolean proxyRequired;

    /**
     * Constructor
     *
     * @param beanManager The Bean manager
     */
    public AbstractBean(BeanAttributes<T> attributes, String idSuffix, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(attributes, idSuffix, beanManager);
        this.injectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.delegateInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.newInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.services = services;
    }

    @Override
    public void cleanupAfterBoot() {
        injectionPoints = immutableSet(injectionPoints);
        delegateInjectionPoints = immutableSet(delegateInjectionPoints);
        newInjectionPoints = immutableSet(newInjectionPoints);
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
                finishSpecialization();
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
        checkDelegateInjectionPoints();
        if (getScope() != null) {
            proxyRequired = Container.instance().services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
        } else {
            proxyRequired = false;
        }
        log.trace(QUALIFIERS_USED, getQualifiers(), this);
        log.trace(USING_NAME, getName(), this);
        log.trace(USING_SCOPE, getScope(), this);
    }

    protected void checkDelegateInjectionPoints() {
        if (this.delegateInjectionPoints.size() > 0) {
            throw new DefinitionException(DELEGATE_NOT_ON_DECORATOR, this);
        }
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        checkType();
    }

    protected abstract void checkType();

    protected void addInjectionPoint(WeldInjectionPoint<?, ?> injectionPoint) {
        if (injectionPoint.isDelegate()) {
            this.delegateInjectionPoints.add(injectionPoint);
        }
        if (injectionPoint.getQualifier(New.class) != null) {
            this.newInjectionPoints.add(injectionPoint);
        }
        injectionPoints.add(injectionPoint);
    }

    protected void addInjectionPoints(Iterable<? extends WeldInjectionPoint<?, ?>> injectionPoints) {
        for (WeldInjectionPoint<?, ?> injectionPoint : injectionPoints) {
            addInjectionPoint(injectionPoint);
        }
    }

    protected Set<WeldInjectionPoint<?, ?>> getDelegateInjectionPoints() {
        return delegateInjectionPoints;
    }

    protected static Set<Type> getTypedTypes(Map<Class<?>, Type> typeClosure, Class<?> rawType, Typed typed) {
        Set<Type> types = new HashSet<Type>();
        for (Class<?> specifiedClass : typed.value()) {
            Type tmp = typeClosure.get(specifiedClass);
            if (tmp != null) {
                types.add(tmp);
            } else {
                throw new DefinitionException(TYPED_CLASS_NOT_IN_HIERARCHY, specifiedClass.getName(), rawType);
            }
        }
        types.add(Object.class);
        return types;
    }

    /**
     * Validates specialization if this bean specializes another bean.
     */
    public void checkSpecialization() {
        if (isSpecializing()) {
            if (getEnhancedAnnotated().isAnnotationPresent(Named.class) && getSpecializedBean().getName() != null) {
                throw new DefinitionException(NAME_NOT_ALLOWED_ON_SPECIALIZATION, getEnhancedAnnotated());
            }
            for (Type type : getSpecializedBean().getTypes()) {
                if (!getTypes().contains(type)) {
                    throw new DefinitionException(SPECIALIZING_BEAN_MISSING_SPECIALIZED_TYPE, this, type, getSpecializedBean());
                }
            }
        }
    }

    protected void postSpecialize() {
        // override qualifiers
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.addAll(attributes().getQualifiers());
        qualifiers.addAll(getSpecializedBean().getQualifiers());
        // override name
        String name = attributes().getName();
        if (isSpecializing() && getSpecializedBean().getName() != null) {
            name = getSpecializedBean().getName();
        }
        setAttributes(new ImmutableBeanAttributes<T>(qualifiers, name, attributes()));
    }

    /**
     * Saves the result of specialization in {@link BeansClosure}.
     */
    protected void finishSpecialization() {
        BeansClosure closure = BeansClosure.getClosure(beanManager);
        closure.addSpecialized(getSpecializedBean(), this);
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

    @Override
    public abstract AbstractBean<?, ?> getSpecializedBean();

    @Override
    public Set<WeldInjectionPoint<?, ?>> getWeldInjectionPoints() {
        return injectionPoints;
    }

    public Set<WeldInjectionPoint<?, ?>> getNewInjectionPoints() {
        return newInjectionPoints;
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
        return Container.instance().services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
    }

    @Override
    public boolean isSpecializing() {
        return getEnhancedAnnotated().isAnnotationPresent(Specializes.class);
    }

    @Override
    public boolean isProxyRequired() {
        return proxyRequired;
    }

    protected ServiceRegistry getServices() {
        return services;
    }
}
