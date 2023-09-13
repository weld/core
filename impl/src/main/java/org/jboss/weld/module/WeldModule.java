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

import java.util.ServiceLoader;

import jakarta.enterprise.context.spi.Context;

import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A Weld module is a unit of code that extends capabilities of Weld. This is done mainly by registering services, beans,
 * contexts and validators.
 * Unlike CDI portable extensions, this SPI is consumed by a limited set of Weld's internal modules that provide integration
 * with Java EE technologies.
 * WeldModules are discovered using {@link ServiceLoader}.
 * This API may change and is for internal use only.
 *
 * @author Jozef Hartinger
 *
 */
public interface WeldModule {

    /**
     * The name of the module.
     *
     * @return
     */
    String getName();

    /**
     * This hook is called once Weld registered {@link Service}s from the deployment and added its own internal Services.
     * A module may use this hook to discover registered per-deployment services and to register additional services.
     *
     * @param ctx context
     */
    default void postServiceRegistration(PostServiceRegistrationContext ctx) {
    }

    /**
     * Context object for the <code>postServiceRegistration</code> phase
     */
    interface PostServiceRegistrationContext {

        /**
         * Returns the container id of the running container
         *
         * @return the container id
         */
        String getContextId();

        /**
         * A mutable service registry. Existing services may be replaced and additional services may be registered by a module.
         *
         * @return services
         */
        ServiceRegistry getServices();

        /**
         * Registers an additional validator to be used for bean validation
         *
         * @param validator
         */
        void registerPlugableValidator(PlugableValidator validator);
    }

    /**
     * This hook is called once Weld registered built-in contexts. A module may register additional contexts.
     *
     * @param ctx context
     */
    default void postContextRegistration(PostContextRegistrationContext ctx) {
    }

    /**
     * Context object for the <code>postContextRegistration</code> phase
     */
    interface PostContextRegistrationContext {

        /**
         * Returns the container id of the running container
         *
         * @return the container id
         */
        String getContextId();

        /**
         * An immutable view on per-deployment services
         *
         * @return services
         */
        ServiceRegistry getServices();

        /**
         * Register an additional context. A built-in bean is automatically registered for each context.
         *
         * @param context CDI context
         */
        void addContext(ContextHolder<? extends Context> context);
    }

    /**
     * This hook is called once Weld registered {@link Service}s for deployment of a particular bean archive.
     * A module may use this hook to discover services for a particular archive and to register additional services.
     * This method is called for each bean archive that is processed.
     *
     * @param ctx context
     */
    default void postBeanArchiveServiceRegistration(PostBeanArchiveServiceRegistrationContext ctx) {
    }

    interface PostBeanArchiveServiceRegistrationContext {
        /**
         * A mutable service registry for a given bean archive. Existing services may be replaced and additional services may be
         * registered by a module.
         *
         * @return services
         */
        ServiceRegistry getServices();

        /**
         * Returns the {@link BeanManagerImpl} for the given bean archive deployment.
         *
         * @return bean manager
         */
        BeanManagerImpl getBeanManager();

        /**
         * Returns the {@link BeanDeploymentArchive} the services are associated with
         *
         * @return the bean deployment archive
         */
        BeanDeploymentArchive getBeanDeploymentArchive();
    }

    /**
     * This hook is called by Weld before it starts deploying beans. A module may register additional built-in beans.
     * This callback is called for each {@link BeanDeployment} separately.
     *
     * @param ctx context
     */
    default void preBeanRegistration(PreBeanRegistrationContext ctx) {
    }

    /**
     * Context object for the <code>preBeanRegistration</code> phase
     */
    interface PreBeanRegistrationContext {
        /**
         * The environment in which Weld is run.
         *
         * @return the environment
         */
        Environment getEnvironment();

        /**
         * Returns {@link BeanDeploymentArchive} represented by this bean archive deployment.
         *
         * @return bda
         */
        BeanDeploymentArchive getBeanDeploymentArchive();

        /**
         * Returns the {@link BeanManagerImpl} for the given bean archive deployment.
         *
         * @return bean manager
         */
        BeanManagerImpl getBeanManager();

        /**
         * Register an additional built-in bean with the given bean archive deployment.
         *
         * @param additional built-in bean
         */
        void registerBean(AbstractBuiltInBean<?> bean);
    }
}
