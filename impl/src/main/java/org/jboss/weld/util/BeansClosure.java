/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

    public void addSpecialized(Bean<?> target, Bean<?> override) {
        addSpecializedInternal(target, override);
        for (BeanManagerImpl accessibleBeanManager : getAccessibleBeanManagers()) {
            BeansClosure closure = accessibleBeanManager.getClosure();
            closure.addSpecializedInternal(target, override);
        }
    }

    protected void addSpecializedInternal(Bean<?> target, Bean<?> override) {
        specialized.put(target, override);
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

    public Bean<?> getSpecialized(Bean<?> bean) {
        return specialized.get(bean);
    }

    public boolean isSpecialized(Bean<?> bean) {
        return getSpecialized(bean) != null;
    }

    public Bean<?> mostSpecialized(Bean<?> bean) {
        Bean most = bean;
        while (most != null) {
            Bean<?> temp = most;
            most = specialized.get(bean);
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
