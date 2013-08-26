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

import static org.jboss.weld.bean.BeanIdentifiers.forManagedBean;
import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.ERROR_DESTROYING;
import static org.jboss.weld.logging.messages.BeanMessage.PASSIVATING_BEAN_HAS_NON_PASSIVATION_CAPABLE_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.PASSIVATING_BEAN_HAS_NON_PASSIVATION_CAPABLE_INTERCEPTOR;
import static org.jboss.weld.logging.messages.BeanMessage.PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL;
import static org.jboss.weld.logging.messages.BeanMessage.PUBLIC_FIELD_ON_NORMAL_SCOPED_BEAN_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Decorator;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.Decorators;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
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
 * @author Marko Luksa
 */
public class ManagedBean<T> extends AbstractClassBean<T> {

    // Logger
    private static final LocLogger log = loggerFactory().getLogger(BEAN);
    private static final XLogger xLog = loggerFactory().getXLogger(BEAN);

    private final boolean proxiable;

    private boolean passivationCapableBean;
    private boolean passivationCapableDependency;

    /**
     * Creates a simple, annotation defined Web Bean
     *
     * @param <T>         The type
     * @param clazz       The class
     * @param beanManager the current manager
     * @return A Web Bean
     */
    public static <T> ManagedBean<T> of(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> clazz, BeanManagerImpl beanManager) {
        BeanIdentifier identifier;
        if (Dependent.class.equals(attributes.getScope()) || ApplicationScoped.class.equals(attributes.getScope())) {
            identifier = new ManagedBeanIdentifier(clazz.slim().getIdentifier());
        } else {
            identifier = new StringBeanIdentifier(forManagedBean(clazz));
        }
        return new ManagedBean<T>(attributes, clazz, identifier, beanManager);
    }

    /**
     * Constructor
     *
     * @param type        The type of the bean
     * @param beanManager The Bean manager
     */
    protected ManagedBean(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, BeanIdentifier identifier, BeanManagerImpl beanManager) {
        super(attributes, type, identifier, beanManager);
        this.proxiable = Proxies.isTypesProxyable(getTypes(), beanManager.getServices());
        setProducer(beanManager.getLocalInjectionTargetFactory(getEnhancedAnnotated()).createInjectionTarget(getEnhancedAnnotated(), this, false));
    }

    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
        initPassivationCapable();
    }

    private void initPassivationCapable() {
        this.passivationCapableBean = getEnhancedAnnotated().isSerializable();
        this.passivationCapableDependency = isNormalScoped() || (isDependent() && passivationCapableBean);
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        if (this.passivationCapableBean && hasDecorators() && !allDecoratorsArePassivationCapable()) {
            this.passivationCapableBean = false;
        }
        if (this.passivationCapableBean && hasInterceptors() && !allInterceptorsArePassivationCapable()) {
            this.passivationCapableBean = false;
        }
        super.initializeAfterBeanDiscovery();
    }

    private boolean allDecoratorsArePassivationCapable() {
        return getFirstNonPassivationCapableDecorator() == null;
    }

    private Decorator<?> getFirstNonPassivationCapableDecorator() {
        for (Decorator<?> decorator : getDecorators()) {
            if (!Decorators.isPassivationCapable(decorator)) {
                return decorator;
            }
        }
        return null;
    }

    private boolean allInterceptorsArePassivationCapable() {
        return getFirstNonPassivationCapableInterceptor() == null;
    }

    private InterceptorMetadata<?> getFirstNonPassivationCapableInterceptor() {
        for (InterceptorMetadata<?> interceptorMetadata : getBeanManager().getInterceptorModelRegistry().get(getType()).getAllInterceptors()) {
            if (!Reflections.isSerializable(interceptorMetadata.getInterceptorClass().getJavaClass())) {
                return interceptorMetadata;
            }
        }
        return null;
    }

    /**
     * Creates an instance of the bean
     *
     * @return The instance
     */
    public T create(CreationalContext<T> creationalContext) {
        T instance = getProducer().produce(creationalContext);
        getProducer().inject(instance, creationalContext);

        if (beanManager.isContextActive(RequestScoped.class)) {
            getProducer().postConstruct(instance);
        } else {
            /*
             * CDI-219
             * The request scope is active during @PostConstruct callback of any bean.
             */
            RequestContext context = getUnboundRequestContext();
            try {
                context.activate();
                getProducer().postConstruct(instance);
            } finally {
                context.invalidate();
                context.deactivate();
            }
        }
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
        boolean passivating = beanManager.isPassivatingScope(getScope());
        if (passivating && !isPassivationCapableBean()) {
            if (!getEnhancedAnnotated().isSerializable()) {
                throw new DeploymentException(PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL, this);
            } else if (hasDecorators() && !allDecoratorsArePassivationCapable()) {
                throw new DeploymentException(PASSIVATING_BEAN_HAS_NON_PASSIVATION_CAPABLE_DECORATOR, this, getFirstNonPassivationCapableDecorator());
            } else if (hasInterceptors() && !allInterceptorsArePassivationCapable()) {
                throw new DeploymentException(PASSIVATING_BEAN_HAS_NON_PASSIVATION_CAPABLE_INTERCEPTOR, this, getFirstNonPassivationCapableInterceptor());
            }
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
    protected void specialize() {
        Set<? extends AbstractBean<?, ?>> specializedBeans = getSpecializedBeans();
        if (specializedBeans.isEmpty()) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        }
        for (AbstractBean<?, ?> specializedBean : specializedBeans) {
            if (!(specializedBean instanceof ManagedBean<?>)) {
                throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
            }
        }
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

    @Override
    public boolean isPassivationCapableBean() {
        return passivationCapableBean;
    }

    @Override
    public boolean isPassivationCapableDependency() {
        return passivationCapableDependency;
    }

    private RequestContext getUnboundRequestContext() {
        final Bean<?> bean = beanManager.resolve(beanManager.getBeans(RequestContext.class, UnboundLiteral.INSTANCE));
        final CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
        return (RequestContext) beanManager.getReference(bean, RequestContext.class, ctx);
    }
}
