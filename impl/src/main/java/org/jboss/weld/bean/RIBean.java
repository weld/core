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

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * Abstract base class with functions specific to RI built-in beans
 *
 * @author Pete Muir
 */
public abstract class RIBean<T> extends CommonBean<T> implements PassivationCapable {

    protected final BeanManagerImpl beanManager;
    private boolean initialized;
    private volatile Set<QualifierInstance> qualifiers;
    private ContextualInstanceStrategy<T> contextualInstanceStrategy;

    protected RIBean(BeanAttributes<T> attributes, BeanIdentifier identifier, BeanManagerImpl beanManager) {
        super(attributes, identifier);
        this.beanManager = beanManager;
        this.contextualInstanceStrategy = ContextualInstanceStrategy.create(attributes, beanManager);
    }

    public BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    public abstract Class<T> getType();

    public Class<?> getBeanClass() {
        return getType();
    }

    public abstract void preInitialize();

    /**
     * Initializes the bean and its metadata. The method is synchronized and guarded by the RIBean object so that the
     * initialization only occurs once.
     *
     */
    public final synchronized void initialize(BeanDeployerEnvironment environment) {
        if (!initialized) {
            internalInitialize(environment);
            initialized = true;
        }
    }

    protected abstract void internalInitialize(BeanDeployerEnvironment environment);

    /**
     * This method is called after the container is started allowing the bean to
     * release any resources that are only required at boot time
     */
    public abstract void cleanupAfterBoot();

    /**
     * In particular cases, the deployer must perform some initialization operations
     * only after all beans have been deployed (e.g. for initializing decorators
     * taking into account the possibility of having custom decorators which are
     * deployed through portable extensions)
     *
     * @param environment
     */
    public abstract void initializeAfterBeanDiscovery();

    public boolean isDependent() {
        return getScope().equals(Dependent.class);
    }

    public boolean isNormalScoped() {
        return getBeanManager().isNormalScope(getScope());
    }

    public abstract boolean isProxyable();

    public abstract boolean isPassivationCapableBean();

    public abstract boolean isPassivationCapableDependency();

    public abstract boolean isProxyRequired();

    public Set<QualifierInstance> getQualifierInstances() {
        if (qualifiers == null) {
            qualifiers = beanManager.getServices().get(MetaAnnotationStore.class).getQualifierInstances(getQualifiers());
        }
        return qualifiers;
    }

    public ContextualInstanceStrategy<T> getContextualInstanceStrategy() {
        return contextualInstanceStrategy;
    }

    @Override
    public void setAttributes(BeanAttributes<T> attributes) {
        super.setAttributes(attributes);
        this.contextualInstanceStrategy = ContextualInstanceStrategy.create(attributes, beanManager);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        contextualInstanceStrategy.destroy(this);
    }
}
