/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.BeanManagers;

/**
 * Keeps the BDA closure information.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class BeansClosure {

    private final BeanManagerImpl owner;
    private volatile Set<BeanManagerImpl> accessibleBeanManagers;

    private final Map<Bean<?>, Bean<?>> specialized = new ConcurrentHashMap<Bean<?>, Bean<?>>();
    private final Map<BeanDeployerEnvironment, Object> envs = new ConcurrentHashMap<BeanDeployerEnvironment, Object>();

    public BeansClosure(BeanManagerImpl owner) {
        this.owner = owner;
    }

    private Set<BeanManagerImpl> getAccessibleBeanManagers() {
        if (accessibleBeanManagers == null) {
            synchronized (this) {
                if (accessibleBeanManagers == null) {
                    Set<BeanManagerImpl> tmp = new HashSet<BeanManagerImpl>();
                    for (Iterable<BeanManagerImpl> beanManagers : BeanManagers.getAccessibleClosure(owner)) {
                        for (BeanManagerImpl accessibleBeanManager : beanManagers) {
                            if (owner != accessibleBeanManager) {
                                tmp.add(accessibleBeanManager);
                            }
                        }
                    }
                    accessibleBeanManagers = tmp;
                }
            }
        }
        return accessibleBeanManagers;
    }

    // --- modification methods

    public void addSpecialized(Bean<?> specializedBean, Bean<?> specializingBean) {
        addSpecializedInternal(specializedBean, specializingBean);
        for (BeanManagerImpl accessibleBeanManager : getAccessibleBeanManagers()) {
            BeansClosure closure = accessibleBeanManager.getClosure();
            closure.addSpecializedInternal(specializedBean, specializingBean);
        }
    }

    protected void addSpecializedInternal(Bean<?> specializedBean, Bean<?> specializingBean) {
        specialized.put(specializedBean, specializingBean);
    }

    public void addEnvironment(BeanDeployerEnvironment environment) {
        addEnvironmentInternal(environment);
        for (BeanManagerImpl accessibleBeanManager : getAccessibleBeanManagers()) {
            BeansClosure closure = accessibleBeanManager.getClosure();
            closure.addEnvironmentInternal(environment);
        }
    }

    protected void addEnvironmentInternal(BeanDeployerEnvironment environment) {
        envs.put(environment, Object.class);
    }

    public void clear() {
        envs.clear();
    }

    public void destroy() {
        specialized.clear();
        if (accessibleBeanManagers != null) {
            accessibleBeanManagers.clear();
        }
    }

    // -- querys

    @Deprecated // should not be used
    public Map getSpecialized() {
        return Collections.unmodifiableMap(specialized);
    }

    public Bean<?> getSpecializingBean(Bean<?> bean) {
        return specialized.get(bean);
    }

    public boolean isSpecialized(Bean<?> bean) {
        return getSpecializingBean(bean) != null;
    }

    public Bean<?> getMostSpecializingBean(Bean<?> bean) {
        Bean most = bean;
        while (most != null) {
            Bean<?> temp = most;
            most = getSpecializingBean(bean);
            bean = temp;
        }
        return bean;
    }

    public boolean isEJB(WeldClass<?> clazz) {
        for (BeanDeployerEnvironment bde : envs.keySet()) {
            EjbDescriptors ed = bde.getEjbDescriptors();
            if (ed.contains(clazz.getJavaClass()))
                return true;
        }
        return false;
    }

    public Bean<?> getClassBean(WeldClass<?> clazz) {
        for (BeanDeployerEnvironment bde : envs.keySet()) {
            AbstractClassBean<?> classBean = bde.getClassBean(clazz);
            if (classBean != null)
                return classBean;
        }
        return null;
    }

    public ProducerMethod<?, ?> getProducerMethod(WeldMethod<?, ?> superClassMethod) {
        for (BeanDeployerEnvironment bde : envs.keySet()) {
            ProducerMethod<?, ?> pm = bde.getProducerMethod(superClassMethod);
            if (pm != null)
                return pm;
        }
        return null;
    }
}
