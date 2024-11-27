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
package org.jboss.weld;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A Weld application container
 *
 * @author pmuir
 */
public class Container {
    public static final String CONTEXT_ID_KEY = "WELD_CONTEXT_ID_KEY";

    private static Singleton<Container> instance;

    private static final AtomicReference<Environment> ENV_REFERENCE = new AtomicReference<>(null);

    static {
        instance = SingletonProvider.instance().create(Container.class);
    }

    /**
     * Get the container for the current application deployment
     *
     * @return
     */
    public static Container instance() {
        return instance.get(RegistrySingletonProvider.STATIC_INSTANCE);
    }

    public static boolean available() {
        return available(RegistrySingletonProvider.STATIC_INSTANCE);
    }

    public static Container instance(String contextId) {
        return instance.get(contextId);
    }

    public static Container instance(BeanManagerImpl manager) {
        return instance(manager.getContextId());
    }

    public static Container instance(AnnotatedTypeIdentifier identifier) {
        return instance(identifier.getContextId());
    }

    public static boolean available(String contextId) {
        return isSet(contextId) && instance(contextId).getState().isAvailable();
    }

    public static boolean isSet(String contextId) {
        return instance.isSet(contextId);
    }

    /**
     * Initialize the container for the current application deployment
     *
     * @param deploymentManager
     * @param deploymentServices
     */
    public static void initialize(BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices) {
        Container instance = new Container(RegistrySingletonProvider.STATIC_INSTANCE, deploymentManager, deploymentServices);
        Container.instance.set(RegistrySingletonProvider.STATIC_INSTANCE, instance);
    }

    public static void initialize(String contextId, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices) {
        initialize(contextId, deploymentManager, deploymentServices, null);
    }

    public static void initialize(String contextId, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices,
            Environment environment) {
        Container instance = new Container(contextId, deploymentManager, deploymentServices, environment);
        Container.instance.set(contextId, instance);
    }

    private final String contextId;

    // The deployment bean manager
    private final BeanManagerImpl deploymentManager;

    // A map of managers keyed by ID, used for activities and serialization
    private final Map<String, BeanManagerImpl> managers;

    // A map of BDA -> bean managers
    private final Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchives;

    private final ServiceRegistry deploymentServices;

    private ContainerState state = ContainerState.STOPPED;

    public Container(String contextId, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices) {
        this(contextId, deploymentManager, deploymentServices, null);
    }

    public Container(BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices) {
        this(RegistrySingletonProvider.STATIC_INSTANCE, deploymentManager, deploymentServices);
    }

    public Container(String contextId, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices,
            Environment environment) {
        this.deploymentManager = deploymentManager;
        this.managers = new ConcurrentHashMap<String, BeanManagerImpl>();
        this.managers.put(deploymentManager.getId(), deploymentManager);
        this.beanDeploymentArchives = new ConcurrentHashMap<BeanDeploymentArchive, BeanManagerImpl>();
        this.deploymentServices = deploymentServices;
        this.contextId = contextId;
        Container.ENV_REFERENCE.compareAndSet(null, environment);
    }

    /**
     * Cause the container to be cleaned up, including all registered bean
     * managers, and all deployment services
     */
    public void cleanup() {
        managers.clear();
        for (BeanManagerImpl beanManager : beanDeploymentArchives.values()) {
            beanManager.cleanup();
        }
        beanDeploymentArchives.clear();
        deploymentServices.cleanup();
        deploymentManager.cleanup();
        instance.clear(contextId);
    }

    /**
     * Gets the manager for this application deployment
     */
    public BeanManagerImpl deploymentManager() {
        return deploymentManager;
    }

    public Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchives() {
        return beanDeploymentArchives;
    }

    /**
     * Get the BeanManager for a given key
     *
     * @param key
     * @return
     */
    public BeanManagerImpl getBeanManager(String key) {
        return managers.get(key);
    }

    private String addBeanManager(BeanManagerImpl manager) {
        String id = manager.getId();
        if (manager.getId() == null) {
            throw BeanManagerLogger.LOG.nullBeanManagerId();
        }
        managers.put(id, manager);
        return id;
    }

    /**
     * Get the services for this application deployment
     *
     * @return the deploymentServices
     */
    public ServiceRegistry services() {
        return deploymentServices;
    }

    /**
     * Add sub-deployment units to the container
     *
     * @param bdaMapping
     */
    public void putBeanDeployments(BeanDeploymentArchiveMapping bdaMapping) {
        for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : bdaMapping.getBdaToBeanManagerMap().entrySet()) {
            beanDeploymentArchives.put(entry.getKey(), entry.getValue());
            addBeanManager(entry.getValue());
        }
    }

    public ContainerState getState() {
        return state;
    }

    public void setState(ContainerState state) {
        this.state = state;
    }

    /**
     * Returns {@link Environment} that this container was booted in, or null if not set or invoked too
     * early in the bootstrapping process.
     *
     * Note that should there be multiple containers active, this will return the {@link Environment} that the first container
     * was bootstrapped in, unless it was null.
     *
     * @return {@link Environment} or null
     */
    public static Environment getEnvironment() {
        return ENV_REFERENCE.get();
    }

}
