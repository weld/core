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
package org.jboss.weld.injection;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.Serializable;

public interface WeldInjectionPoint<T, S> extends InjectionPoint, WeldAnnotated<T, S> {

    abstract static class WeldInjectionPointSerializationProxy<T, S> implements Serializable {

        private static final long serialVersionUID = -5488095196637387378L;

        private final String declaringBeanId;
        private final Class<?> declaringClass;
        private final WeldManager beanManager;

        public WeldInjectionPointSerializationProxy(WeldInjectionPoint<T, S> injectionPoint, WeldManager beanManager) {
            this.declaringBeanId =
                    injectionPoint.getBean() == null ? null : getService(ContextualStore.class).putIfAbsent(injectionPoint.getBean());
            this.declaringClass = injectionPoint.getDeclaringType().getJavaClass();
            this.beanManager = beanManager;
        }

        protected Bean<T> getDeclaringBean() {
            return declaringBeanId == null ? null : getService(ContextualStore.class).<Bean<T>, T>getContextual(declaringBeanId);
        }

        protected WeldClass<?> getDeclaringWeldClass() {
            return getService(ClassTransformer.class).loadClass(declaringClass);
        }

        protected String getDeclaringBeanId() {
            return declaringBeanId;
        }

        protected WeldManager getBeanManager() {
            return beanManager;
        }

        protected <E extends Service> E getService(Class<E> serviceClass) {
            return beanManager.getServices().get(serviceClass);
        }

    }

    WeldClass<?> getDeclaringType();

    /**
     * Injects an instance
     *
     * @param declaringInstance The instance to inject into
     * @param value             The value to inject
     */
    void inject(Object declaringInstance, Object value);

}
