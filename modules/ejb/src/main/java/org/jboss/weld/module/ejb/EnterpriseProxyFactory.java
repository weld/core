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

package org.jboss.weld.module.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.weld.bean.proxy.CommonProxiedMethodFilters;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.bytecode.MethodInformation;
import org.jboss.weld.util.bytecode.RuntimeMethodInformation;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * This factory produces client proxies specific for enterprise beans, in
 * particular session beans. It adds the interface
 * {@link EnterpriseBeanInstance} on the proxy.
 *
 * @author David Allen
 */
class EnterpriseProxyFactory<T> extends ProxyFactory<T> {

    private static final String SUFFIX = "$EnterpriseProxy$";

    /**
     * Produces a factory for a specific bean implementation.
     *
     * @param proxiedBeanType the actual enterprise bean
     */
    EnterpriseProxyFactory(Class<T> proxiedBeanType, SessionBeanImpl<T> bean) {
        super(bean.getBeanManager().getContextId(), proxiedBeanType, ImmutableSet.<Type> builder().addAll(bean.getTypes())
                .addAll(bean.getEjbDescriptor().getRemoteBusinessInterfacesAsClasses()).build(), bean);
    }

    @Override
    protected void addSpecialMethods(ClassFile proxyClassType, ClassMethod staticConstructor) {
        super.addSpecialMethods(proxyClassType, staticConstructor);

        // Add methods for the EnterpriseBeanInstance interface
        try {
            proxyClassType.addInterface(EnterpriseBeanInstance.class.getName());
            for (Method method : EnterpriseBeanInstance.class.getMethods()) {
                BeanLogger.LOG.addingMethodToEnterpriseProxy(method);
                MethodInformation methodInfo = new RuntimeMethodInformation(method);
                createInterceptorBody(proxyClassType.addMethod(method), methodInfo, staticConstructor);
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    @Override
    protected String getProxyNameSuffix() {
        return SUFFIX;
    }

    @Override
    protected boolean isMethodAccepted(Method method, Class<?> proxySuperclass) {
        return super.isMethodAccepted(method, proxySuperclass)
                && CommonProxiedMethodFilters.NON_PRIVATE.accept(method, proxySuperclass);
    }

    @Override
    protected boolean isUsingProxyInstantiator() {
        return false;
    }
}
