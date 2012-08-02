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

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.logging.LoggerFactory;
import org.jboss.weld.logging.MessageConveyorFactory;
import org.jboss.weld.manager.BeanManagerImpl;

import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_BEAN_MANAGER_ID;

/**
 * A Weld application container
 *
 * @author pmuir
 * @author alesj
 */
public class Container {
    public static final String CONTEXT_ID_KEY = "WELD_CONTEXT_ID_KEY";

    public static final ThreadLocal<String> currentId = new ThreadLocal<String>();

    private static Singleton<Container> instance;

    static {
        instance = SingletonProvider.instance().create(Container.class);
    }

    /**
     * Get the container for the current application deployment
     *
     * @return static instance
     */
    public static Container instance() {
        return instance(RegistrySingletonProvider.STATIC_INSTANCE);
    }

    public static boolean available() {
        return available(RegistrySingletonProvider.STATIC_INSTANCE);
    }

    public static Container instance(String contextId) {
        return instance.get(contextId);
    }

    public static boolean available(String contextId) {
        return instance.isSet(contextId) && instance(contextId) != null && instance(contextId).getState().isAvailable();
    }

    /**
     * Initialize the container for the current application deployment
     *
     * @param deploymentManager the deployment manager
     * @param deploymentServices the deployment services
     */
    public static void initialize(BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices) {
        initialize(RegistrySingletonProvider.STATIC_INSTANCE, deploymentManager, deploymentServices);
    }

    public static void initialize(String contextId, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices) {
        Container instance = new Container(contextId, deploymentManager, deploymentServices);
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
        this.deploymentManager = deploymentManager;
        this.managers = new ConcurrentHashMap<String, BeanManagerImpl>();
        this.managers.put(deploymentManager.getId(), deploymentManager);
        this.beanDeploymentArchives = new ConcurrentHashMap<BeanDeploymentArchive, BeanManagerImpl>();
        this.deploymentServices = deploymentServices;
        this.contextId = contextId;
    }

    public Container(BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices) {
        this(RegistrySingletonProvider.STATIC_INSTANCE, deploymentManager, deploymentServices);
    }

    /**
     * Cause the container to be cleaned up, including all registered bean
     * managers, and all deployment services
     */
    public void cleanup() {
        // TODO We should probably cleanup the bean managers for activities?
        managers.clear();
        for (BeanManagerImpl beanManager : beanDeploymentArchives.values()) {
            beanManager.cleanup();
        }
        beanDeploymentArchives.clear();
        deploymentServices.cleanup();
        deploymentManager.cleanup();
        LoggerFactory.cleanup();
        MessageConveyorFactory.cleanup();
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
     * Get the activity manager for a given key
     *
     * @param key the key
     * @return activity manager
     */
    public BeanManagerImpl activityManager(String key) {
        return managers.get(key);
    }

    /**
     * Add an activity
     *
     * @param manager the bean manager
     * @return key
     */
    public String addActivity(BeanManagerImpl manager) {
        String id = manager.getId();
        if (manager.getId() == null) {
            throw new IllegalArgumentException(NULL_BEAN_MANAGER_ID, manager);
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
     * @param beanDeployments bean deployments
     */
    public void putBeanDeployments(Map<BeanDeploymentArchive, BeanDeployment> beanDeployments) {
        for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet()) {
            beanDeploymentArchives.put(entry.getKey(), entry.getValue().getBeanManager());
            addActivity(entry.getValue().getBeanManager());
        }
    }

    public ContainerState getState() {
        return state;
    }

    public void setState(ContainerState state) {
        this.state = state;
    }

}
