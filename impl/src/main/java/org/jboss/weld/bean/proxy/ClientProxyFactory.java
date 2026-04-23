/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

// Old jboss-classfilewriter imports removed - now using Gizmo 2
import org.jboss.weld.Container;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.proxy.WeldClientProxy;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.bytecode.DeferredBytecode;
import org.jboss.weld.util.reflection.Reflections;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * Proxy factory that generates client proxies, it uses optimizations that
 * are not valid for other proxy types.
 *
 * @author Stuart Douglas
 * @author Marius Bogoevici
 */
public class ClientProxyFactory<T> extends ProxyFactory<T> {

    private static final String CLIENT_PROXY_SUFFIX = "ClientProxy";

    /**
     * It is possible although very unlikely that two different beans will end up with the same proxy class
     * (generally this will only happen in test situations where weld is being started/stopped multiple times
     * in the same class loader, such as during unit tests)
     * <p/>
     * To avoid this causing serialization problems we explicitly set the bean id on creation, and store it in this
     * field.
     */
    private static final String BEAN_ID_FIELD = "BEAN_ID_FIELD";
    private static final String CONTEXT_ID_FIELD = "CONTEXT_ID_FIELD";

    private final BeanIdentifier beanId;

    private volatile Field beanIdField;
    private volatile Field contextIdField;

    public ClientProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        super(contextId, proxiedBeanType, typeClosure, bean);
        beanId = Container.instance(contextId).services().get(ContextualStore.class).putIfAbsent(bean);
    }

    @Override
    public T create(BeanInstance beanInstance) {
        try {
            final T instance = super.create(beanInstance);
            if (beanIdField == null) {
                final Field f = instance.getClass().getDeclaredField(BEAN_ID_FIELD);
                Reflections.ensureAccessible(f);
                beanIdField = f;
            }
            if (contextIdField == null) {
                final Field f = instance.getClass().getDeclaredField(CONTEXT_ID_FIELD);
                Reflections.ensureAccessible(f);
                contextIdField = f;
            }
            beanIdField.set(instance, beanId);
            contextIdField.set(instance, getContextId());
            return instance;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    protected void addAdditionalInterfaces(Set<Class<?>> interfaces) {
        // add marker interface for client proxy, this also requires adding interface methods implementations
        interfaces.add(WeldClientProxy.class);
    }

    @Override
    protected void addMethods(ClassCreator cc) {
        // delegate to ProxyFactory#addMethods
        super.addMethods(cc);

        // add method from WeldClientProxy
        generateWeldClientProxyMethod(cc);
    }

    private void generateWeldClientProxyMethod(ClassCreator cc) {
        try {
            Method getContextualMetadata = WeldClientProxy.class.getMethod("getMetadata");
            MethodDesc methodDesc = MethodDesc.of(getContextualMetadata);
            cc.method(methodDesc, m -> {
                m.public_();
                m.body(b -> {
                    // ProxyMethodHandler implements ContextualMetadata, so let's just return reference to it
                    FieldDesc methodHandlerField = FieldDesc.of(
                            cc.type(), METHOD_HANDLER_FIELD_NAME, getMethodHandlerType());
                    Expr handler = b.get(m.this_().field(methodHandlerField));
                    b.return_(handler);
                });
            });
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    @Override
    protected void addFields(ClassCreator cc, List<DeferredBytecode> initialValueBytecode) {
        super.addFields(cc, initialValueBytecode);

        cc.field(BEAN_ID_FIELD, f -> {
            f.setType(BeanIdentifier.class);
            f.private_();
            f.addFlag(io.quarkus.gizmo2.creator.ModifierFlag.VOLATILE);
        });

        cc.field(CONTEXT_ID_FIELD, f -> {
            f.setType(String.class);
            f.private_();
            f.addFlag(io.quarkus.gizmo2.creator.ModifierFlag.VOLATILE);
        });
    }

    @Override
    protected Class<? extends MethodHandler> getMethodHandlerType() {
        return ProxyMethodHandler.class;
    }

    @Override
    protected void addSerializationSupport(ClassCreator cc) {
        // Serialization support not yet implemented for Gizmo 2
        // The client proxy serialization requires creating new SerializableClientProxy instances
        // which needs proper object instantiation support in Gizmo 2
        // For now, client proxies using Gizmo 2 won't be serializable
        // This will be implemented in a future update
    }

    // OLD METHODS REMOVED - NOT CALLED BY GIZMO 2 CODE PATH
    // createForwardingMethodBody, loadBeanInstance, generateHashCodeMethod, generateEqualsMethod
    // These will be reimplemented if needed when ClientProxyFactory is fully migrated to Gizmo 2

    @Override
    protected String getProxyNameSuffix() {
        return CLIENT_PROXY_SUFFIX;
    }

    @Override
    protected boolean isMethodAccepted(Method method, Class<?> proxySuperclass) {
        return super.isMethodAccepted(method, proxySuperclass)
                && CommonProxiedMethodFilters.NON_PRIVATE.accept(method, proxySuperclass);
    }
}
