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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CREATING_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_NOT_ON_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.MULTIPLE_SCOPES_FOUND_FROM_STEREOTYPES;
import static org.jboss.weld.logging.messages.BeanMessage.NAME_NOT_ALLOWED_ON_SPECIALIZATION;
import static org.jboss.weld.logging.messages.BeanMessage.QUALIFIERS_USED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MISSING_SPECIALIZED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.TYPED_CLASS_NOT_IN_HIERARCHY;
import static org.jboss.weld.logging.messages.BeanMessage.USING_DEFAULT_NAME;
import static org.jboss.weld.logging.messages.BeanMessage.USING_DEFAULT_QUALIFIER;
import static org.jboss.weld.logging.messages.BeanMessage.USING_NAME;
import static org.jboss.weld.logging.messages.BeanMessage.USING_SCOPE_FROM_STEREOTYPE;

/**
 * An abstract bean representation common for all beans
 *
 * @param <T> the type of bean
 * @param <S> the Class<?> of the bean type
 * @author Pete Muir
 * @author Ales Justin
 */
public abstract class AbstractBean<T, S> extends RIBean<T> {

    private static final LocLogger log = loggerFactory().getLogger(BEAN);
    protected Set<Annotation> qualifiers;
    protected String name;
    protected Class<? extends Annotation> scope;
    private MergedStereotypes<T, S> mergedStereotypes;
    protected boolean alternative;
    protected Class<T> type;
    protected Set<Type> types;
    private ArraySet<WeldInjectionPoint<?, ?>> injectionPoints;
    private ArraySet<WeldInjectionPoint<?, ?>> delegateInjectionPoints;
    private ArraySet<WeldInjectionPoint<?, ?>> newInjectionPoints;
    protected BeanManagerImpl beanManager;
    private final ServiceRegistry services;
    private boolean initialized;
    private boolean proxyRequired;

    /**
     * Constructor
     *
     * @param beanManager The Bean manager
     */
    public AbstractBean(String idSuffix, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(idSuffix, beanManager);
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
     * Initializes the bean and its metadata
     */
    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        initialized = true;
        if (isSpecializing()) {
            preSpecialize(environment);
            specialize(environment);
            postSpecialize();
        }
        initDefaultQualifiers();
        log.trace(CREATING_BEAN, getType());
        initName();
        initScope();
        checkDelegateInjectionPoints();
        if (getScope() != null) {
            proxyRequired = Container.instance(beanManager.getContextId()).services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
        } else {
            proxyRequired = false;
        }
        this.qualifiers = Collections.unmodifiableSet(new ArraySet<Annotation>(qualifiers));
    }

    protected void initStereotypes() {
        mergedStereotypes = new MergedStereotypes<T, S>(getWeldAnnotated().getMetaAnnotations(Stereotype.class), beanManager);
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

    protected void initTypes() {
        if (getWeldAnnotated().isAnnotationPresent(Typed.class)) {
            this.types = Collections.unmodifiableSet(new ArraySet<Type>(getTypedTypes(Reflections.buildTypeMap(getWeldAnnotated().getTypeClosure()), getWeldAnnotated().getJavaClass(), getWeldAnnotated().getAnnotation(Typed.class))));
        } else {
            if (getType().isInterface()) {
                this.types = new ArraySet<Type>(getWeldAnnotated().getTypeClosure());
                this.types.add(Object.class);
                this.types = Collections.unmodifiableSet(this.types);
            } else {
                this.types = getWeldAnnotated().getTypeClosure();
            }
        }
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

    protected void initQualifiers() {
        this.qualifiers = new HashSet<Annotation>();
        qualifiers.addAll(getWeldAnnotated().getMetaAnnotations(Qualifier.class));
        initDefaultQualifiers();
        log.trace(QUALIFIERS_USED, qualifiers, this);
    }

    protected void initDefaultQualifiers() {
        if (qualifiers.size() == 0) {
            log.trace(USING_DEFAULT_QUALIFIER, this);
            this.qualifiers.add(DefaultLiteral.INSTANCE);
        }
        if (qualifiers.size() == 1) {
            if (qualifiers.iterator().next().annotationType().equals(Named.class)) {
                log.trace(USING_DEFAULT_QUALIFIER, this);
                this.qualifiers.add(DefaultLiteral.INSTANCE);
            }
        }
        this.qualifiers.add(AnyLiteral.INSTANCE);

        // fix found Named, to have full name binding value
        boolean foundRemoved = false;
        Iterator<Annotation> qIter = qualifiers.iterator();
        while (qIter.hasNext()) {
            Annotation next = qIter.next();
            if (next.annotationType().equals(Named.class)) {
                Named named = (Named) next;
                if (named.value().length() == 0) {
                    qIter.remove();
                    foundRemoved = true;
                }
                break;
            }
        }
        if (foundRemoved) {
            Named named = new NamedLiteral(getDefaultName());
            qualifiers.add(named);
        }
    }

    protected void initAlternative() {
        this.alternative = Beans.isAlternative(getWeldAnnotated(), getMergedStereotypes());
    }

    /**
     * Initializes the name
     */
    protected void initName() {
        boolean beanNameDefaulted = false;
        if (getWeldAnnotated().isAnnotationPresent(Named.class)) {
            String javaName = getWeldAnnotated().getAnnotation(Named.class).value();
            if ("".equals(javaName)) {
                beanNameDefaulted = true;
            } else {
                log.trace(USING_NAME, javaName, this);
                this.name = javaName;
                return;
            }
        }

        if (beanNameDefaulted || getMergedStereotypes().isBeanNameDefaulted()) {
            this.name = getDefaultName();
            log.trace(USING_DEFAULT_NAME, name, this);
        }
    }

    protected abstract void initScope();

    protected boolean initScopeFromStereotype() {
        Set<Annotation> possibleScopes = getMergedStereotypes().getPossibleScopes();
        if (possibleScopes.size() == 1) {
            this.scope = possibleScopes.iterator().next().annotationType();
            log.trace(USING_SCOPE_FROM_STEREOTYPE, scope, this, getMergedStereotypes());
            return true;
        } else if (possibleScopes.size() > 1) {
            throw new DefinitionException(MULTIPLE_SCOPES_FOUND_FROM_STEREOTYPES, getWeldAnnotated());
        } else {
            return false;
        }
    }

    protected void postSpecialize() {
        if (getWeldAnnotated().isAnnotationPresent(Named.class) && getSpecializedBean().getWeldAnnotated().isAnnotationPresent(Named.class)) {
            throw new DefinitionException(NAME_NOT_ALLOWED_ON_SPECIALIZATION, getWeldAnnotated());
        }
        for (Type type : getSpecializedBean().getTypes()) {
            if (!getTypes().contains(type)) {
                throw new DefinitionException(SPECIALIZING_BEAN_MISSING_SPECIALIZED_TYPE, this, type, getSpecializedBean());
            }
        }
        this.qualifiers.addAll(getSpecializedBean().getQualifiers());
        if (isSpecializing() && getSpecializedBean().getWeldAnnotated().isAnnotationPresent(Named.class)) {
            this.name = getSpecializedBean().getName();
        }
        BeansClosure closure = beanManager.getClosure();
        closure.addSpecialized(getSpecializedBean(), this);
    }

    protected void preSpecialize(BeanDeployerEnvironment environment) {

    }

    protected void specialize(BeanDeployerEnvironment environment) {

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
        return qualifiers;
    }

    /**
     * Gets the default name of the bean
     *
     * @return The default name
     */
    protected abstract String getDefaultName();

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
     * Gets the merged stereotypes of the bean
     *
     * @return The set of merged stereotypes
     */
    protected MergedStereotypes<T, S> getMergedStereotypes() {
        return mergedStereotypes;
    }

    /**
     * Gets the name of the bean
     *
     * @return The name
     * @see org.jboss.weld.bean.RIBean#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the scope type of the bean
     *
     * @return The scope type
     * @see org.jboss.weld.bean.RIBean#getScope()
     */
    public Class<? extends Annotation> getScope() {
        return scope;
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
        return types;
    }

    /**
     * Indicates if bean is nullable
     *
     * @return True if nullable, false otherwise
     * @see org.jboss.weld.bean.RIBean#isNullable()
     */
    public boolean isNullable() {
        return !isPrimitive();
    }

    /**
     * Indicates if bean type is a primitive
     *
     * @return True if primitive, false otherwise
     */
    @Override
    public boolean isPrimitive() {
        return getWeldAnnotated().isPrimitive();
    }

    @Override
    public boolean isDependent() {
        return Dependent.class.equals(getScope());
    }

    public boolean isNormalScoped() {
        return Container.instance(beanManager.getContextId()).services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
    }

    public boolean isAlternative() {
        return alternative;
    }

    @Override
    public boolean isSpecializing() {
        return getWeldAnnotated().isAnnotationPresent(Specializes.class);
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return mergedStereotypes.getStereotypes();
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

    /**
     * Returns true if the bean uses the default {@link Producer} ( or {@link InjectionTarget}). The method returns false if the
     * producer of the bean was replaced by an extension.
     */
    public abstract boolean hasDefaultProducer();
}
