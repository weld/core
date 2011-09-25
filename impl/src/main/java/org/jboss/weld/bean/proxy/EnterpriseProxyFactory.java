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

import javassist.bytecode.ClassFile;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.util.bytecode.MethodInformation;
import org.jboss.weld.util.bytecode.MethodUtils;
import org.jboss.weld.util.bytecode.RuntimeMethodInformation;

import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.Method;

/**
 * This factory produces client proxies specific for enterprise beans, in
 * particular session beans. It adds the interface
 * {@link EnterpriseBeanInstance} on the proxy.
 *
 * @author David Allen
 */
public class EnterpriseProxyFactory<T> extends ProxyFactory<T> {
    /**
     * Produces a factory for a specific bean implementation.
     *
     * @param proxiedBeanType the actual enterprise bean
     */
    public EnterpriseProxyFactory(String contextId, Class<T> proxiedBeanType, Bean<T> bean) {
        super(contextId, proxiedBeanType, bean.getTypes(), bean);
    }

    @Override
    protected void addSpecialMethods(ClassFile proxyClassType) {
        super.addSpecialMethods(proxyClassType);

        // Add methods for the EnterpriseBeanInstance interface
        try {
            proxyClassType.addInterface(EnterpriseBeanInstance.class.getName());
            for (Method method : EnterpriseBeanInstance.class.getDeclaredMethods()) {
                log.trace("Adding method " + method);
                MethodInformation methodInfo = new RuntimeMethodInformation(method);
                proxyClassType.addMethod(MethodUtils.makeMethod(methodInfo, method.getExceptionTypes(), createInterceptorBody(proxyClassType, methodInfo), proxyClassType.getConstPool()));
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }

    }
}
