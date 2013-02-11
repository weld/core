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

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_ID_CREATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.CREATED_NEW_CLIENT_PROXY_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.LOOKED_UP_CLIENT_PROXY;
import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.slf4j.cal10n.LocLogger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A proxy pool for holding scope adaptors (client proxies)
 *
 * @author Nicklas Karlsson
 * @see org.jboss.weld.bean.proxy.ProxyMethodHandler
 */
public class ClientProxyProvider {
    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private static final Object BEAN_NOT_PROXYABLE_MARKER = new Object();
    private static final CacheLoader<Bean<Object>, Object> CREATE_BEAN_TYPE_CLOSURE_CLIENT_PROXY = new CacheLoader<Bean<Object>, Object>() {
        @Override
        public Object load(Bean<Object> from) {
            if (Proxies.isTypesProxyable(from)) {
                return createClientProxy(from);
            } else {
                return BEAN_NOT_PROXYABLE_MARKER;
            }
        }
    };

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

    private static final CacheLoader<RequestedTypeHolder, Object> CREATE_REQUESTED_TYPE_CLOSURE_CLIENT_PROXY = new CacheLoader<ClientProxyProvider.RequestedTypeHolder, Object>() {
        @Override
        public Object load(RequestedTypeHolder input) {
            Set<Type> requestedTypeClosure = Container.instance().services().get(SharedObjectCache.class).getTypeClosureHolder(input.requestedType).get();
            if (Proxies.isTypesProxyable(requestedTypeClosure)) {
                return createClientProxy(input.bean, requestedTypeClosure);
            } else {
                return BEAN_NOT_PROXYABLE_MARKER;
            }
        }
    };

    /**
     * A container/cache for previously created proxies
     *
     * @author Nicklas Karlsson
     */
    private final LoadingCache<Bean<Object>, Object> beanTypeClosureProxyPool;
    private final LoadingCache<RequestedTypeHolder, Object> requestedTypeClosureProxyPool;

    /**
     * Constructor
     */
    public ClientProxyProvider() {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        this.beanTypeClosureProxyPool = cacheBuilder.build(CREATE_BEAN_TYPE_CLOSURE_CLIENT_PROXY);
        this.requestedTypeClosureProxyPool = cacheBuilder.build(CREATE_REQUESTED_TYPE_CLOSURE_CLIENT_PROXY);
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
    private static <T> T createClientProxy(Bean<T> bean) throws RuntimeException {
        return createClientProxy(bean, bean.getTypes());
    }

    private static <T> T createClientProxy(Bean<T> bean, Set<Type> types) {
        String id = Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
        if (id == null) {
            throw new DefinitionException(BEAN_ID_CREATION_FAILED, bean);
        }
        ContextBeanInstance<T> beanInstance = new ContextBeanInstance<T>(bean, id);
        TypeInfo typeInfo = TypeInfo.of(types);
        T proxy = new ClientProxyFactory<T>(typeInfo.getSuperClass(), types, bean).create(beanInstance);
        log.trace(CREATED_NEW_CLIENT_PROXY_TYPE, proxy.getClass(), bean, id);
        return proxy;
    }

    public <T> T getClientProxy(final Bean<T> bean) {
        T proxy = getCastCacheValue(beanTypeClosureProxyPool, bean);
        if (proxy == BEAN_NOT_PROXYABLE_MARKER) {
            throw Proxies.getUnproxyableTypesException(bean);
        }
        log.trace(LOOKED_UP_CLIENT_PROXY, proxy.getClass(), bean);
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
        T proxy = getCastCacheValue(beanTypeClosureProxyPool, bean);
        if (proxy == BEAN_NOT_PROXYABLE_MARKER) {
            /*
             *  the bean may have a type that is not proxyable - this is not a problem as long as the unproxyable
             *  type is not in the type closure of the requested type
             *  https://issues.jboss.org/browse/WELD-1052
             */
            proxy = getCastCacheValue(requestedTypeClosureProxyPool, new RequestedTypeHolder(requestedType, bean));
            if (proxy == BEAN_NOT_PROXYABLE_MARKER) {
                throw Proxies.getUnproxyableTypeException(requestedType);
            }
        }
        log.trace(LOOKED_UP_CLIENT_PROXY, proxy.getClass(), bean);
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
        this.beanTypeClosureProxyPool.invalidateAll();
        this.requestedTypeClosureProxyPool.invalidateAll();
    }

}
