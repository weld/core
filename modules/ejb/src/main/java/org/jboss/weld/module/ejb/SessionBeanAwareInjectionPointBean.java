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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.InjectionPointBean;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * <p>
 * The spec requires that an {@link InjectionPoint} of a session bean returns:
 * </p>
 *
 * <ul>
 * <li>a {@link Bean} object representing the session bean if the session bean is a contextual instance (obtained using
 * {@link Inject})</li>
 * <li>null if the session bean is a non-contextual instance obtained using @EJB or from JNDI</li>
 * </ul>
 *
 * <p>
 * Each time a contextual instance of a session bean is created we add its bean class to a thread local collection. The class is
 * removed from the collection after instance creation.
 * <p>
 *
 * <p>
 * If an {@link InjectionPoint} of a session bean is about to be injected using {@link InjectionPointBean} and the session bean
 * class is present in the thread local collection (indicating that it is a contextual instance), we leave the
 * {@link InjectionPoint} untouched and inject it. Otherwise, we wrap the {@link InjectionPoint} within
 * {@link NonContextualSessionBeanInjectionPoint} which always returns null from getBean().
 * </p>
 *
 * <p>
 * The only known limitation of this approach is that if the same session bean is used both in its contextual and non-contextual
 * form within a single dependency chain (non-contextual instance of Foo injecting another Foo instance using {@link Inject}),
 * the behavior will not be reliable.
 * </p>
 *
 * @author Jozef Hartinger
 *
 */
class SessionBeanAwareInjectionPointBean extends InjectionPointBean {

    SessionBeanAwareInjectionPointBean(BeanManagerImpl manager) {
        super(manager);
    }

    @Override
    protected InjectionPoint newInstance(InjectionPoint ip, CreationalContext<InjectionPoint> creationalContext) {
        ip = super.newInstance(ip, creationalContext);
        if (ip != null) {
            ip = SessionBeanAwareInjectionPointBean.wrapIfNecessary(ip);
        }
        return ip;
    }

    private static final ThreadLocal<Set<Class<?>>> CONTEXTUAL_SESSION_BEANS = new ThreadLocal<Set<Class<?>>>() {
        @Override
        protected Set<Class<?>> initialValue() {
            return new HashSet<Class<?>>();
        }
    };

    /**
     * Indicates that a contextual instance of a session bean is about to be constructed.
     */
    public static void registerContextualInstance(EjbDescriptor<?> descriptor) {
        CONTEXTUAL_SESSION_BEANS.get().add(descriptor.getBeanClass());
    }

    /**
     * Indicates that contextual session bean instance has been constructed.
     */
    public static void unregisterContextualInstance(EjbDescriptor<?> descriptor) {
        Set<Class<?>> classes = CONTEXTUAL_SESSION_BEANS.get();
        classes.remove(descriptor.getBeanClass());
        if (classes.isEmpty()) {
            CONTEXTUAL_SESSION_BEANS.remove();
        }
    }

    /**
     * Returns the {@link InjectionPoint} passed in as a parameter if this {@link InjectionPoint} belongs to a contextual
     * instance of a session bean. Otherwise, the method wraps the {@link InjectionPoint} to guarantee that getBean() always
     * returns null.
     */
    public static InjectionPoint wrapIfNecessary(InjectionPoint ip) {
        if (ip.getBean() instanceof SessionBean<?>) {
            if (!CONTEXTUAL_SESSION_BEANS.get().contains(ip.getBean().getBeanClass())) {
                return new NonContextualSessionBeanInjectionPoint(ip);
            }
        }
        return ip;
    }

    private static class NonContextualSessionBeanInjectionPoint extends ForwardingInjectionPoint implements Serializable {

        private static final long serialVersionUID = 6338875301221129389L;

        private final InjectionPoint delegate;

        public NonContextualSessionBeanInjectionPoint(InjectionPoint delegate) {
            this.delegate = delegate;
        }

        @Override
        protected InjectionPoint delegate() {
            return delegate;
        }

        @Override
        public Bean<?> getBean() {
            return null;
        }
    }
}
