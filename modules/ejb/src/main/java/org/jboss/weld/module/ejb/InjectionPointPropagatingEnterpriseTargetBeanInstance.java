/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.io.ObjectStreamException;
import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance;
import org.jboss.weld.bean.proxy.MethodHandler;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.EmptyInjectionPoint;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.InjectionPointHolder;

/**
 * @author Marko Luksa
 * @author Jozef Hartinger
 *
 */
class InjectionPointPropagatingEnterpriseTargetBeanInstance extends EnterpriseTargetBeanInstance {

    private static final long serialVersionUID = 166825647603520280L;

    private final InjectionPointHolder injectionPointHolder;
    private final String contextId;
    private transient CurrentInvocationInjectionPoint currentInvocationInjectionPoint;

    InjectionPointPropagatingEnterpriseTargetBeanInstance(Class<?> baseType, MethodHandler methodHandler,
            BeanManagerImpl manager) {
        super(baseType, methodHandler);
        this.contextId = manager.getContextId();
        this.currentInvocationInjectionPoint = manager.getServices().get(CurrentInvocationInjectionPoint.class);
        InjectionPoint ip = manager.getServices().get(CurrentInjectionPoint.class).peek();
        if (ip != null) {
            this.injectionPointHolder = new InjectionPointHolder(manager.getContextId(), ip);
        } else {
            this.injectionPointHolder = null;
        }
    }

    @Override
    public Object invoke(Object instance, Method method, Object... arguments) throws Throwable {
        ThreadLocalStackReference<InjectionPoint> stack = null;
        if (injectionPointHolder != null) {
            stack = currentInvocationInjectionPoint.push(injectionPointHolder.get());
        } else {
            stack = currentInvocationInjectionPoint.push(EmptyInjectionPoint.INSTANCE);
        }

        try {
            return super.invoke(instance, method, arguments);
        } finally {
            stack.pop();
        }
    }

    private Object readResolve() throws ObjectStreamException {
        this.currentInvocationInjectionPoint = Container.instance(contextId).services()
                .get(CurrentInvocationInjectionPoint.class);
        return this;
    }
}
