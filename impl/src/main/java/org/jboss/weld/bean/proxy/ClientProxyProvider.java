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
package org.jboss.weld.bean.proxy;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.Container;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.ServiceLoader;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * A proxy pool for holding scope adaptors (client proxies)
 *
 * @author Nicklas Karlsson
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 * @see org.jboss.weld.bean.proxy.ProxyMethodHandler
 */
public class ClientProxyProvider {

    private static final Object BEAN_NOT_PROXYABLE_MARKER = new Object();

    private class CreateClientProxy implements Function<Bean<Object>, Object> {
        @Override
        public Object apply(Bean<Object> from) {
            if (Proxies.isTypesProxyable(from, services())) {
                return createClientProxy(from);
            } else {
                return BEAN_NOT_PROXYABLE_MARKER;
            }
        }
    }

    private class CreateClientProxyForType implements Function<RequestedTypeHolder, Object> {
        @Override
        public Object apply(RequestedTypeHolder input) {
            // First, collect all interfaces
            ImmutableSet.Builder<Type> types = ImmutableSet.builder();
            for (Type type : input.bean.getTypes()) {
                if (Reflections.getRawType(type).isInterface()) {
                    types.add(type);
                }
            }
            // Object.class as a required type is often used for lookup if no required type is available (e.g. integration with frameworks where
            // only names are used to reference beans). In this case, try to use the bean class (or type of a producer) instead of Object.
            if (input.requestedType.equals(Object.class)) {
                Type beanType;
                if (input.bean instanceof RIBean) {
                    RIBean<?> riBean = (RIBean<?>) input.bean;
                    beanType = riBean.getType();
                } else {
                    beanType = input.bean.getBeanClass();
                }
                if (Proxies.isTypeProxyable(beanType, services())) {
                    return createClientProxy(input.bean, types.add(beanType).build());
                }
            }
            // If the requested type if proxyable use requested type + bean interfaces
            if (Proxies.isTypeProxyable(input.requestedType, services())) {
                return createClientProxy(input.bean, types.add(input.requestedType).build());
            }
            /*
             * Requested type is not proxyable. Check whether a proxyable subtype exists within the set of bean types that we could use instead.
             */
            Class<?> requestedRawType = Reflections.getRawType(input.requestedType);
            for (Type type : input.bean.getTypes()) {
                if (requestedRawType.isAssignableFrom(Reflections.getRawType(type)) && Proxies.isTypeProxyable(type, services())) {
                    return createClientProxy(input.bean, types.add(type).build());
                }
            }
            return BEAN_NOT_PROXYABLE_MARKER;
        }
    }

    private static class RequestedTypeHolder {
        private final Type requestedType;
        private final Bean<?> bean;

        private RequestedTypeHolder(Type requestedType, Bean<?> bean) {
            this.requestedType = requestedType;
            this.bean = bean;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((bean == null) ? 0 : bean.hashCode());
            result = prime * result + ((requestedType == null) ? 0 : requestedType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RequestedTypeHolder other = (RequestedTypeHolder) obj;
            if (bean == null) {
                if (other.bean != null) {
                    return false;
                }
            } else if (!bean.equals(other.bean)) {
                return false;
            }
            if (requestedType == null) {
                if (other.requestedType != null) {
                    return false;
                }
            } else if (!requestedType.equals(other.requestedType)) {
                return false;
            }
            return true;
        }
    }

    /**
     * A container/cache for previously created proxies
     *
     * @author Nicklas Karlsson
     */
    private final ComputingCache<Bean<Object>, Object> beanTypeClosureProxyPool;
    private final ComputingCache<RequestedTypeHolder, Object> requestedTypeClosureProxyPool;

    private String containerId;
    private volatile ServiceRegistry services;

    /**
     * Creates a new {@link ClientProxyProvider}.
     *
     * <p>This constructor should be invoked only by subclasses and
     * (indirectly) the {@link #newInstance(ResourceLoader, String)}
     * method, which will ensure that a proper {@linkplain
     * #getContainerId() container identifier} has been set.</p>
     *
     * @see #newInstance(ResourceLoader, String)
     *
     * @see #ClientProxyProvider(String)
     */
    protected ClientProxyProvider() {
        this(null);
    }

    /**
     * Creates a new {@link ClientProxyProvider}.
     *
     * <p>Consider using the {@link #newInstance(ResourceLoader,
     * String)} method instead.</p>
     *
     * @param containerId the identifier of the {@link Container} on
     * behalf of which this {@link ClientProxyProvider} is being
     * created
     */
    public ClientProxyProvider(String containerId) {
        ComputingCacheBuilder cacheBuilder = ComputingCacheBuilder.newBuilder();
        this.beanTypeClosureProxyPool = cacheBuilder.build(new CreateClientProxy());
        this.requestedTypeClosureProxyPool = cacheBuilder.build(new CreateClientProxyForType());
        this.setContainerId(containerId);
    }

    private ServiceRegistry services() {
        if (services == null) {
            synchronized (this) {
                if (services == null) {
                    this.services = Container.instance(this.getContainerId()).services();
                }
            }
        }
        return this.services;
    }

    private void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    /**
     * Returns the identifier of the {@link Container} on behalf of
     * which this {@link ClientProxyProvider} is being created.
     *
     * @return the container identifier
     */
    protected final String getContainerId() {
        return this.containerId;
    }

    /**
     * Creates a Javassist scope adaptor (client proxy) for a bean
     * <p/>
     * Creates a Javassist proxy factory. Gets the type info. Sets the interfaces
     * and superclass to the factory. Hooks in the MethodHandler and creates the
     * proxy.
     *
     * @param bean      The bean to proxy
     * @param beanIndex The index to the bean in the manager bean list
     * @return A Javassist proxy
     * @throws InstantiationException When the proxy couldn't be created
     * @throws IllegalAccessException When the proxy couldn't be created
     */
    private <T> T createClientProxy(Bean<T> bean) throws RuntimeException {
        return createClientProxy(bean, bean.getTypes());
    }

    private <T> T createClientProxy(Bean<T> bean, Set<Type> types) {
        final String containerId = this.getContainerId();
        BeanIdentifier id = this.services().get(ContextualStore.class).putIfAbsent(bean);
        if (id == null) {
            throw BeanLogger.LOG.beanIdCreationFailed(bean);
        }
        BeanInstance beanInstance = new ContextBeanInstance<T>(bean, id, containerId);
        TypeInfo typeInfo = TypeInfo.of(types);
        T proxy = new ClientProxyFactory<T>(containerId, typeInfo.getSuperClass(), types, bean).create(beanInstance);
        BeanLogger.LOG.createdNewClientProxyType(proxy.getClass(), bean, id);
        return proxy;
    }

    public <T> T getClientProxy(final Bean<T> bean) {
        T proxy = beanTypeClosureProxyPool.getCastValue(bean);
        if (proxy == BEAN_NOT_PROXYABLE_MARKER) {
            throw Proxies.getUnproxyableTypesException(bean, services());
        }
        BeanLogger.LOG.lookedUpClientProxy(proxy.getClass(), bean);
        return proxy;
    }
    /**
     * Gets a client proxy for a bean
     * <p/>
     * Looks for a proxy in the pool. If not found, one is created and added to
     * the pool if the create argument is true.
     *
     * @param bean The bean to get a proxy to
     * @return the client proxy for the bean
     */
    public <T> T getClientProxy(final Bean<T> bean, Type requestedType) {
        // let's first try to use the proxy that implements all the bean types
        T proxy = beanTypeClosureProxyPool.getCastValue(bean);
        if (proxy == BEAN_NOT_PROXYABLE_MARKER) {
            /*
             *  the bean may have a type that is not proxyable - this is not a problem as long as the unproxyable
             *  type is not in the type closure of the requested type
             *  https://issues.jboss.org/browse/WELD-1052
             */
            proxy = requestedTypeClosureProxyPool.getCastValue(new RequestedTypeHolder(requestedType, bean));
            if (proxy == BEAN_NOT_PROXYABLE_MARKER) {
                throw Proxies.getUnproxyableTypeException(requestedType, services());
            }
        }
        BeanLogger.LOG.lookedUpClientProxy(proxy.getClass(), bean);
        return proxy;
    }


    /**
     * Gets a string representation
     *
     * @return The string representation
     */
    @Override
    public String toString() {
        return "Proxy pool with " + beanTypeClosureProxyPool.size() + " bean type proxies and " + requestedTypeClosureProxyPool.size() + "injection point type proxies.";
    }

    public void clear() {
        this.beanTypeClosureProxyPool.clear();
        this.requestedTypeClosureProxyPool.clear();
    }

    /**
     * Creates and returns a new {@link ClientProxyProvider} instance.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * <p>This method uses the {@link ServiceLoader#load(Class,
     * ResourceLoader)} method internally to find the appropriate
     * subclass to instantiate.  If there is a problem instantiating
     * such a subclass, or if no service provider has been designated
     * at all, a {@linkplain #ClientProxyProvider(String) new
     * <code>ClientProxyProvider</code>} is returned instead (the most
     * common case).</p>
     *
     * <p>It is guaranteed that an invocation of the {@link
     * #getContainerId()} method on the returned {@link
     * ClientProxyProvider} will return a {@link String} {@linkplain
     * String#equals(Object) equal to} the supplied {@code
     * containerId}.</p>
     *
     * @param resourceLoader a {@link ResourceLoader} that will be
     * used by a {@link ServiceLoader}; may be {@code null}
     *
     * @param containerId the identifier of the {@link Container} on
     * behalf of which the new {@link ClientProxyProvider} is being
     * built; may be {@code null} but behavior of the returned {@link
     * ClientProxyProvider} is undefined in this case
     *
     * @return a new, non-{@code null} {@link ClientProxyProvider}
     *
     * @see #getContainerId()
     *
     * @see ServiceLoader#load(Class, ResourceLoader)
     */
    public static ClientProxyProvider newInstance(final ResourceLoader resourceLoader, final String containerId) {
        final ClientProxyProvider returnValue;
        final Iterator<Metadata<ClientProxyProvider>> iterator = ServiceLoader.load(ClientProxyProvider.class, resourceLoader).iterator();
        if (iterator != null && iterator.hasNext()) {
            returnValue = iterator.next().getValue();
            returnValue.setContainerId(containerId);
        } else {
            returnValue = new ClientProxyProvider(containerId);
        }
        return returnValue;
    }

}
