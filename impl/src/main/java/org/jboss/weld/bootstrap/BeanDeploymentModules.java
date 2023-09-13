/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.WeldCollections;

/**
 * Repository for {@link BeanDeploymentModule}s. It is an optional per-deployment service, i.e. it does not make sense for all
 * environments (e.g. Weld SE).
 *
 * @author Jozef Hartinger
 *
 */
public final class BeanDeploymentModules implements Service, Iterable<BeanDeploymentModule> {

    private final ConcurrentMap<String, BeanDeploymentModule> modules;
    private final BeanDeploymentModule defaultModule;

    BeanDeploymentModules(String contextId, ServiceRegistry services) {
        this.defaultModule = new BeanDeploymentModule(BeanDeploymentModules.class.getSimpleName() + ".DEFAULT", contextId,
                false, services);
        this.modules = new ConcurrentHashMap<>();
    }

    public BeanDeploymentModule getModule(BeanManagerImpl manager) {
        final EEModuleDescriptor descriptor = manager.getServices().get(EEModuleDescriptor.class);
        // fallback for legacy integrators
        if (descriptor == null) {
            defaultModule.addManager(manager);
            modules.putIfAbsent(defaultModule.getId(), defaultModule);
            return defaultModule;
        }
        BeanDeploymentModule module = modules.get(descriptor.getId());
        if (module == null) {
            module = new BeanDeploymentModule(descriptor.getId(), manager.getContextId(),
                    descriptor.getType() == EEModuleDescriptor.ModuleType.WEB, manager.getServices());
            module = WeldCollections.putIfAbsent(modules, descriptor.getId(), module);
        }
        module.addManager(manager);
        return module;
    }

    public void processBeanDeployments(Iterable<BeanDeployment> deployments) {
        for (BeanDeployment deployment : deployments) {
            getModule(deployment.getBeanManager());
        }
    }

    @Override
    public void cleanup() {
        modules.clear();
    }

    @Override
    public Iterator<BeanDeploymentModule> iterator() {
        return modules.values().iterator();
    }

    @Override
    public String toString() {
        return modules.values().toString();
    }
}
