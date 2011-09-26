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

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.jboss.weld.Container;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import javax.enterprise.inject.spi.Bean;
import java.util.concurrent.ConcurrentMap;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_ID_CREATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.CREATED_NEW_CLIENT_PROXY_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.LOOKED_UP_CLIENT_PROXY;

/**
 * A proxy pool for holding scope adaptors (client proxies)
 *
 * @author Nicklas Karlsson
 * @see org.jboss.weld.bean.proxy.ProxyMethodHandler
 */
public class ClientProxyProvider {
    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private Function<Bean<Object>, Object> createClientProxy;

    private static final class CreateClientProxy implements Function<Bean<Object>, Object> {

        private final String contextId;

        public CreateClientProxy(String contextId) {
            this.contextId = contextId;
        }

        public Object apply(Bean<Object> from) {

            String id = Container.instance(contextId).services().get(ContextualStore.class).putIfAbsent(from);
            if (id == null) {
                throw new DefinitionException(BEAN_ID_CREATION_FAILED, from);
            }
            return createClientProxy(contextId, from, id);
        }
    }

    /**
     * A container/cache for previously created proxies
     *
     * @author Nicklas Karlsson
     */
    private final ConcurrentMap<Bean<Object>, Object> pool;

    /**
     * Constructor
     */
    public ClientProxyProvider(String contextId) {
        createClientProxy = new CreateClientProxy(contextId);
        this.pool = new MapMaker().makeComputingMap(createClientProxy);
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
    private static <T> T createClientProxy(String contextId, Bean<T> bean, String id) throws RuntimeException {
        ContextBeanInstance<T> beanInstance = new ContextBeanInstance<T>(bean, id, contextId);
        TypeInfo typeInfo = TypeInfo.of(bean.getTypes());
        T proxy = new ClientProxyFactory<T>(contextId, typeInfo.getSuperClass(), bean.getTypes(), bean).create(beanInstance);
        log.trace(CREATED_NEW_CLIENT_PROXY_TYPE, proxy.getClass(), bean, id);
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
    public <T> T getClientProxy(final Bean<T> bean) {
        T proxy = Reflections.<T>cast(pool.get(bean));
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
        return "Proxy pool with " + pool.size() + " proxies";
    }

    public void clear() {
        this.pool.clear();
    }

}
