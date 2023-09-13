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
package org.jboss.weld.module;

import static org.jboss.weld.util.ServiceLoader.load;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.spi.Context;

import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.ServiceRegistries;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.WeldModule.PostBeanArchiveServiceRegistrationContext;
import org.jboss.weld.module.WeldModule.PostContextRegistrationContext;
import org.jboss.weld.module.WeldModule.PostServiceRegistrationContext;
import org.jboss.weld.module.WeldModule.PreBeanRegistrationContext;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * This service takes core of {@link WeldModule}s registered with Weld.
 *
 * @author Jozef Hartinger
 *
 */
public class WeldModules implements Service {

    private final List<WeldModule> modules;
    private Set<PlugableValidator> validators;

    public WeldModules() {
        modules = load(WeldModule.class, WeldClassLoaderResourceLoader.INSTANCE).stream().map(metadata -> metadata.getValue())
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                .collect(ImmutableList.collector());
        this.validators = Collections.emptySet();
        BootstrapLogger.LOG.debugv("Using Weld modules: {0}",
                modules.stream().map(m -> m.getName()).collect(Collectors.toList()));
    }

    public void postServiceRegistration(final String contextId, final ServiceRegistry services) {
        final Set<PlugableValidator> validators = new HashSet<>();
        final PostServiceRegistrationContext ctx = new PostServiceRegistrationContext() {
            @Override
            public ServiceRegistry getServices() {
                return services;
            }

            @Override
            public String getContextId() {
                return contextId;
            }

            @Override
            public void registerPlugableValidator(PlugableValidator validator) {
                validators.add(validator);
            }
        };
        for (WeldModule module : modules) {
            module.postServiceRegistration(ctx);
        }
        this.validators = ImmutableSet.copyOf(validators);
    }

    public void postContextRegistration(final String contextId, final ServiceRegistry services,
            final List<ContextHolder<? extends Context>> contexts) {
        final PostContextRegistrationContext ctx = new PostContextRegistrationContext() {
            @Override
            public String getContextId() {
                return contextId;
            }

            @Override
            public ServiceRegistry getServices() {
                return ServiceRegistries.unmodifiableServiceRegistry(services);
            }

            @Override
            public void addContext(ContextHolder<? extends Context> context) {
                contexts.add(context);
            }
        };
        for (WeldModule module : modules) {
            module.postContextRegistration(ctx);
        }
    }

    public void postBeanArchiveServiceRegistration(final ServiceRegistry services, final BeanManagerImpl manager,
            final BeanDeploymentArchive archive) {
        final PostBeanArchiveServiceRegistrationContext ctx = new PostBeanArchiveServiceRegistrationContext() {

            @Override
            public ServiceRegistry getServices() {
                return services;
            }

            @Override
            public BeanManagerImpl getBeanManager() {
                return manager;
            }

            @Override
            public BeanDeploymentArchive getBeanDeploymentArchive() {
                return archive;
            }
        };
        for (WeldModule module : modules) {
            module.postBeanArchiveServiceRegistration(ctx);
        }
    }

    public void preBeanRegistration(final BeanDeployment deployment, final Environment environment) {
        final PreBeanRegistrationContext ctx = new PreBeanRegistrationContext() {
            @Override
            public void registerBean(AbstractBuiltInBean<?> bean) {
                deployment.getBeanDeployer().addBuiltInBean(bean);
            }

            @Override
            public Environment getEnvironment() {
                return environment;
            }

            @Override
            public BeanManagerImpl getBeanManager() {
                return deployment.getBeanManager();
            }

            @Override
            public BeanDeploymentArchive getBeanDeploymentArchive() {
                return deployment.getBeanDeploymentArchive();
            }
        };
        for (WeldModule module : modules) {
            module.preBeanRegistration(ctx);
        }
    }

    @Override
    public void cleanup() {
    }

    public Set<PlugableValidator> getPluggableValidators() {
        return validators;
    }
}
