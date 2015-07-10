/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.AbstractCDI;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.environment.ContainerInstance;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.events.ContainerShutdown;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.literal.DestroyedLiteral;
import org.jboss.weld.literal.InitializedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.util.collections.ImmutableList;

/**
 * Represents a Weld SE container.
 *
 * <p>
 * An new instance can be initialized using the Weld builder:
 * </p>
 *
 * <pre>
 * WeldContainer container = new Weld().initialize();
 * </pre>
 *
 * <p>
 * It's also possible to obtain the instance of a running container by id:
 * </p>
 *
 * <pre>
 * WeldContainer container = WeldContainer.instance(&quot;myCustomId&quot;);
 * </pre>
 *
 * <p>
 * {@link #shutdown()} must be always called to shutdown the container properly. AutoCloseable is implemented, so the container is automatically shut down when
 * leaving the try-with-resources block:
 * </p>
 *
 * <pre>
 * try (WeldContainer container = new Weld().initialize()) {
 *     container.select(Foo.class).get();
 * }
 * </pre>
 *
 * <p>
 * The container is also registered as a {@link javax.inject.Singleton} bean.
 * </p>
 *
 * <p>
 * Provides convenient access to beans, BeanManager and events, which is particularly helpful when bootstrapping an application in Java SE:
 * </p>
 *
 * <pre>
 * Foo foo = container.select(Foo.class).get();
 * container.getBeanManager().fireEvent(new Bar())
 * container.event().select(Bar.class).fire(new Bar());
 * </pre>
 *
 * @author Peter Royle
 * @author Martin Kouba
 * @see Weld
 */
@Vetoed
public class WeldContainer extends AbstractCDI<Object> implements AutoCloseable, ContainerInstance {

    private static final Singleton<WeldContainer> SINGLETON;

    private static final List<String> RUNNING_CONTAINER_IDS;

    private static final Object LOCK = new Object();

    private static volatile ShutdownHook shutdownHook;

    static {
        SINGLETON = SingletonProvider.instance().create(WeldContainer.class);
        RUNNING_CONTAINER_IDS = new CopyOnWriteArrayList<String>();
    }

    /**
     * @param id
     * @return the running container with the specified identifier or <code>null</code> if no such container exists
     */
    public static WeldContainer instance(String id) {
        try {
            return SINGLETON.get(id);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     *
     * @return an immutable list of ids of running containers
     */
    public static List<String> getRunningContainerIds() {
        return ImmutableList.copyOf(RUNNING_CONTAINER_IDS);
    }

    /**
     *
     * @param id
     * @param manager
     * @param bootstrap
     * @return the initialized Weld container
     */
    static WeldContainer initialize(String id, WeldManager manager, Bootstrap bootstrap) {
        if (SINGLETON.isSet(id)) {
            throw WeldSELogger.LOG.weldContainerAlreadyRunning(id);
        }
        WeldContainer weldContainer = new WeldContainer(id, manager, bootstrap);
        SINGLETON.set(id, weldContainer);
        RUNNING_CONTAINER_IDS.add(id);
        WeldSELogger.LOG.weldContainerInitialized(id);
        manager.fireEvent(new ContainerInitialized(id), InitializedLiteral.APPLICATION);
        // We register only one shutdown hook for all containers
        if (shutdownHook == null) {
            synchronized (LOCK) {
                if (shutdownHook == null) {
                    shutdownHook = new ShutdownHook();
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                }
            }
        }
        return weldContainer;
    }

    // This replicates org.jboss.weld.Container.contextId
    private final String id;

    private final WeldManager manager;

    private final Bootstrap bootstrap;

    private final Instance<Object> instance;

    private final Event<Object> event;

    private final CreationalContext<?> creationalContext;

    /**
     *
     * @param id
     * @param manager
     * @param bootstrap
     */
    private WeldContainer(String id, WeldManager manager, Bootstrap bootstrap) {
        super();
        this.id = id;
        this.manager = manager;
        this.bootstrap = bootstrap;
        this.creationalContext = manager.createCreationalContext(null);
        BeanManagerImpl beanManagerImpl = ((BeanManagerImpl) manager.unwrap());
        this.instance = beanManagerImpl.getInstance(creationalContext);
        this.event = beanManagerImpl.event();
    }

    /**
     * Provides access to all beans within the application. Retained for backward compatibility.
     *
     * @return the instance
     */
    public Instance<Object> instance() {
        return instance;
    }

    /**
     * Provides access to all events within the application. For example:
     *
     * <code>
     * weldContainer.event().select(Bar.class).fire(new Bar());
     * </code>
     *
     * @return the event
     */
    public Event<Object> event() {
        return event;
    }

    /**
     * Weld containers must have a unique identifier assigned when there are multiple Weld instances running at once.
     *
     * @return the container id
     */
    public String getId() {
        return id;
    }

    /**
     * Provides direct access to the BeanManager.
     */
    public BeanManager getBeanManager() {
        return manager;
    }

    /**
     * Shutdown the container.
     */
    public synchronized void shutdown() {
        if (isRunning()) {
            try {
                manager.fireEvent(new ContainerShutdown(id), DestroyedLiteral.APPLICATION);
            } finally {
                SINGLETON.clear(id);
                RUNNING_CONTAINER_IDS.remove(id);
                // Destroy all the dependent beans correctly
                creationalContext.release();
                bootstrap.shutdown();
                WeldSELogger.LOG.weldContainerShutdown(id);
            }
        } else {
            WeldSELogger.LOG.weldContainerAlreadyShutDown(id);
            if (WeldSELogger.LOG.isTraceEnabled()) {
                WeldSELogger.LOG.tracev("Spurious call to shutdown from: {0}", (Object[]) Thread.currentThread().getStackTrace());
            }
        }
    }

    /**
     *
     * @return <code>true</code> if the container was not shut down yet, <code>false</code> otherwise
     */
    public boolean isRunning() {
        return SINGLETON.isSet(id);
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    protected Instance<Object> getInstance() {
        return instance;
    }

    /**
     * Shuts down all running containers.
     */
    static class ShutdownHook extends Thread {

        @Override
        public void run() {
            for (String id : getRunningContainerIds()) {
                WeldContainer container = instance(id);
                if (container != null) {
                    container.shutdown();
                    // At this time the logger service may not be available - print some basic info to the standard output
                    System.out.println(String.format("Weld SE container %s shut down by shutdown hook", id));
                }
            }
        }
    }

}