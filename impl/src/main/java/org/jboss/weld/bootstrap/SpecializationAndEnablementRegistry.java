/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.manager.BeanManagerImpl;

import com.google.common.base.Function;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * Holds information about specialized beans.
 *
 * @author Jozef Hartinger
 *
 */
public class SpecializationAndEnablementRegistry implements Service {

    private class SpecializedBeanResolverForBeanManager implements Function<BeanManagerImpl, SpecializedBeanResolver> {

        @Override
        public SpecializedBeanResolver apply(BeanManagerImpl manager) {
            return new SpecializedBeanResolver(buildAccessibleBeanDeployerEnvironments(manager));
        }

        private Set<BeanDeployerEnvironment> buildAccessibleBeanDeployerEnvironments(BeanManagerImpl manager) {
            Set<BeanDeployerEnvironment> result = new HashSet<BeanDeployerEnvironment>();
            result.add(environmentByManager.get(manager));
            buildAccessibleBeanDeployerEnvironments(manager, result);
            return result;
        }

        private void buildAccessibleBeanDeployerEnvironments(BeanManagerImpl manager, Collection<BeanDeployerEnvironment> result) {
            for (BeanManagerImpl accessibleManager : manager.getAccessibleManagers()) {
                BeanDeployerEnvironment environment = environmentByManager.get(accessibleManager);
                if (!result.contains(environment)) {
                    result.add(environment);
                    buildAccessibleBeanDeployerEnvironments(accessibleManager, result);
                }
            }
        }
    }

    private class BeansSpecializedByBean implements Function<Bean<?>, Set<? extends AbstractBean<?, ?>>> {

        @Override
        public Set<? extends AbstractBean<?, ?>> apply(Bean<?> specializingBean) {
            Set<? extends AbstractBean<?, ?>> result = null;
            if (specializingBean instanceof AbstractClassBean<?>) {
                result = apply((AbstractClassBean<?>) specializingBean);
            }
            if (specializingBean instanceof ProducerMethod<?, ?>) {
                result = apply((ProducerMethod<?, ?>) specializingBean);
            }
            if (result != null) {
                if (isEnabledInAnyBeanDeployment(specializingBean)) {
                    specializedBeansSet.addAll(result);
                }
                return result;
            }
            throw new IllegalArgumentException("Unsupported bean type " + specializingBean);
        }

        private Set<AbstractClassBean<?>> apply(AbstractClassBean<?> bean) {
            return getSpecializedBeanResolver(bean).resolveSpecializedBeans(bean);
        }

        private Set<ProducerMethod<?, ?>> apply(ProducerMethod<?, ?> bean) {
            return getSpecializedBeanResolver(bean).resolveSpecializedBeans(bean);
        }

        private SpecializedBeanResolver getSpecializedBeanResolver(RIBean<?> bean) {
            return specializedBeanResolvers.get(bean.getBeanManager());
        }
    }

    private final Map<BeanManagerImpl, SpecializedBeanResolver> specializedBeanResolvers;
    private final Map<BeanManagerImpl, BeanDeployerEnvironment> environmentByManager = new ConcurrentHashMap<BeanManagerImpl, BeanDeployerEnvironment>();
    // maps specializing beans to the set of specialized beans
    private final ConcurrentMap<Bean<?>, Set<? extends AbstractBean<?, ?>>> specializedBeans;
    // fast lookup structure that allows us to figure out if a given bean is specialized in any of the bean deployments
    private final Multiset<AbstractBean<?, ?>> specializedBeansSet = ConcurrentHashMultiset.create();

    public SpecializationAndEnablementRegistry() {
        this.specializedBeanResolvers = new MapMaker().makeComputingMap(new SpecializedBeanResolverForBeanManager());
        this.specializedBeans = new MapMaker().makeComputingMap(new BeansSpecializedByBean());
    }

    /**
     * Returns a set of beans specialized by this bean. An empty set is returned if this bean does not specialize another beans.
     */
    public Set<? extends AbstractBean<?, ?>> resolveSpecializedBeans(Bean<?> specializingBean) {
        if (specializingBean instanceof AbstractClassBean<?>) {
            AbstractClassBean<?> abstractClassBean = (AbstractClassBean<?>) specializingBean;
            if (abstractClassBean.isSpecializing()) {
                return specializedBeans.get(specializingBean);
            }
        }
        if (specializingBean instanceof ProducerMethod<?, ?>) {
            ProducerMethod<?, ?> producerMethod = (ProducerMethod<?, ?>) specializingBean;
            if (producerMethod.isSpecializing()) {
                return specializedBeans.get(specializingBean);
            }
        }
        return Collections.emptySet();
    }

    public void vetoSpecializingBean(Bean<?> bean) {
        Set<? extends AbstractBean<?, ?>> noLongerSpecializedBeans = this.specializedBeans.remove(bean);
        if (noLongerSpecializedBeans != null) {
            for (AbstractBean<?, ?> noLongerSpecializedBean : noLongerSpecializedBeans) {
                specializedBeansSet.remove(noLongerSpecializedBean);
            }
        }
    }

    public boolean isSpecializedInAnyBeanDeployment(Bean<?> bean) {
        return specializedBeansSet.contains(bean);
    }

    public boolean isEnabledInAnyBeanDeployment(Bean<?> bean) {
        for (BeanManagerImpl manager : environmentByManager.keySet()) {
            if (manager.isBeanEnabled(bean)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCandidateForLifecycleEvent(Bean<?> bean) {
        if (bean instanceof AbstractProducerBean<?, ?, ?>) {
            AbstractProducerBean<?, ?, ?> producer = cast(bean);
            if (!isCandidateForLifecycleEvent(producer.getDeclaringBean())) {
                return false;
            }
        }
        return isEnabledInAnyBeanDeployment(bean) && !isSpecializedInAnyBeanDeployment(bean);
    }

    public void registerEnvironment(BeanManagerImpl manager, BeanDeployerEnvironment environment) {
        if (!specializedBeanResolvers.isEmpty()) {
            throw new IllegalStateException(this.getClass().getName() + ".registerEnvironment() must not be called after specialization resolution begins");
        }
        if (environment == null) {
            throw new IllegalArgumentException("Environment must not be null");
        }
        this.environmentByManager.put(manager, environment);
    }

    @Override
    public void cleanup() {
        specializedBeanResolvers.clear();
        environmentByManager.clear();
        specializedBeans.clear();
        specializedBeansSet.clear();
    }

    public Set<AbstractBean<?, ?>> getBeansSpecializedInAnyDeployment() {
        return specializedBeansSet.elementSet();
    }

    public Multiset<AbstractBean<?, ?>> getBeansSpecializedInAnyDeploymentAsMultiset() {
        return Multisets.unmodifiableMultiset(specializedBeansSet);
    }
}
