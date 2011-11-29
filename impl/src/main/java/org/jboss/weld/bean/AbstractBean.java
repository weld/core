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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.inject.Named;

import org.jboss.weld.Container;
import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldAnnotated;
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
    protected BeanAttributes<T> attributes;

    private ArraySet<WeldInjectionPoint<?, ?>> injectionPoints;
    private ArraySet<WeldInjectionPoint<?, ?>> delegateInjectionPoints;
    private ArraySet<WeldInjectionPoint<?, ?>> newInjectionPoints;
    protected BeanManagerImpl beanManager;
    private final ServiceRegistry services;
    private boolean initialized;
    private boolean dirty = true;
    private boolean proxyRequired;

    /**
     * Constructor
     *
     * @param beanManager The Bean manager
     */
    public AbstractBean(BeanAttributes<T> attributes, String idSuffix, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(idSuffix, beanManager);
        this.attributes = attributes;
        this.beanManager = beanManager;
        this.injectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.delegateInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.newInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.services = services;
    }

    @Override
    public void cleanupAfterBoot() {
        injectionPoints.trimToSize();
        delegateInjectionPoints.trimToSize();
        newInjectionPoints.trimToSize();
    }

    /**
     * Initializes specialization. This method is called before {@link ProcessBeanAttributes} is fired and also after the event
     * if the {@link BeanAttributes} have been altered.
     */
    @Override
    public void preInitialize() {
        if (isSpecializing() && isDirty()) {
            preSpecialize();
            specialize();
            postSpecialize();
            dirty = false;
        }
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        preInitialize();
        initialized = true;
        if (isSpecializing()) {
            finishSpecialization();
        }
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
        if (injectionPoint.isAnnotationPresent(Delegate.class)) {
            this.delegateInjectionPoints.add(injectionPoint);
        }
        if (injectionPoint.isAnnotationPresent(New.class)) {
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

    protected void postSpecialize() {
        if (getWeldAnnotated().isAnnotationPresent(Named.class) && getSpecializedBean().getName() != null) {
            throw new DefinitionException(NAME_NOT_ALLOWED_ON_SPECIALIZATION, getWeldAnnotated());
        }
        for (Type type : getSpecializedBean().getTypes()) {
            if (!getTypes().contains(type)) {
                throw new DefinitionException(SPECIALIZING_BEAN_MISSING_SPECIALIZED_TYPE, this, type, getSpecializedBean());
            }
        }
        // override qualifiers
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.addAll(attributes.getQualifiers());
        qualifiers.addAll(getSpecializedBean().getQualifiers());
        // override name
        String name = attributes.getName();
        if (isSpecializing() && getSpecializedBean().getName() != null) {
            name = getSpecializedBean().getName();
        }
        this.attributes = new ImmutableBeanAttributes<T>(qualifiers, name, attributes);
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
    public abstract WeldAnnotated<T, S> getWeldAnnotated();

    /**
     * Gets the binding types
     *
     * @return The set of binding types
     * @see org.jboss.weld.bean.RIBean#getQualifiers()
     */
    public Set<Annotation> getQualifiers() {
        return attributes.getQualifiers();
    }

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
     * Gets the name of the bean
     *
     * @return The name
     * @see org.jboss.weld.bean.RIBean#getName()
     */
    public String getName() {
        return attributes.getName();
    }

    /**
     * Gets the scope type of the bean
     *
     * @return The scope type
     * @see org.jboss.weld.bean.RIBean#getScope()
     */
    public Class<? extends Annotation> getScope() {
        return attributes.getScope();
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

    /**
     * Gets the API types of the bean
     *
     * @return The set of API types
     * @see org.jboss.weld.bean.RIBean#getTypes()
     */
    public Set<Type> getTypes() {
        return attributes.getTypes();
    }

    /**
     * Indicates if bean is nullable
     *
     * @return True if nullable, false otherwise
     * @see org.jboss.weld.bean.RIBean#isNullable()
     */
    public boolean isNullable() {
        return attributes.isNullable();
    }

    @Override
    public boolean isDependent() {
        return Dependent.class.equals(getScope());
    }

    public boolean isNormalScoped() {
        return Container.instance().services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
    }

    public boolean isAlternative() {
        return attributes.isAlternative();
    }

    @Override
    public boolean isSpecializing() {
        return getWeldAnnotated().isAnnotationPresent(Specializes.class);
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return attributes.getStereotypes();
    }

    protected boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isProxyRequired() {
        return proxyRequired;
    }

    protected ServiceRegistry getServices() {
        return services;
    }

    public BeanAttributes<T> getAttributes() {
        return attributes;
    }

    public void setAttributes(BeanAttributes<T> attributes) {
        this.attributes = attributes;
    }

    /**
     * Mark this bean to be reinitialized.
     */
    public void setDirty() {
        dirty = true;
    }

    /**
     * Used during bootstrap to indicate specialization of the bean should be reinitialized after {@link ProcessBeanAttributes}
     * has been fired.
     */
    public boolean isDirty() {
        for (AbstractBean<?, ?> bean = this; bean != null; bean = bean.getSpecializedBean()) {
            if (bean.dirty) {
                return true;
            }
        }
        return false;
    }
}
