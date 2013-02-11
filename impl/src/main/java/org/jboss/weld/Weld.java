/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.messages.BeanManagerMessage;
import org.jboss.weld.manager.BeanManagerImpl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Provides convenient way to access the CDI container.
 *
 * @author Jozef Hartinger
 *
 */
public class Weld extends CDI<Object> {

    private class ClassNameToBeanManager extends CacheLoader<String, BeanManagerProxy> {

        /**
         * Determines the correct {@link BeanManagerImpl} based on a class name of the caller.
         */
        @Override
        public BeanManagerProxy load(String callerClassName) {
            return new BeanManagerProxy(findBeanManager(callerClassName));
        }

        public BeanManagerImpl findBeanManager(String callerClassName) {
            if (callerClassName == null) {
                throw new IllegalStateException(BeanManagerMessage.UNABLE_TO_IDENTIFY_BEAN_MANAGER);
            }
            Container container = Container.instance();
            Set<BeanManagerImpl> managers = new HashSet<BeanManagerImpl>();
            for (Map.Entry<BeanDeploymentArchive, BeanManagerImpl> entry : container.beanDeploymentArchives().entrySet()) {
                for (String className : entry.getKey().getBeanClasses()) {
                    if (className.equals(callerClassName)) {
                        managers.add(entry.getValue());
                    }
                }
            }

            if (managers.size() == 1) {
                return managers.iterator().next();
            }
            if (managers.size() == 0) {
                return unsatisfiedBeanManager(callerClassName);
            }
            return ambiguousBeanManager(callerClassName, managers);
        }
    }

    private final LoadingCache<String, BeanManagerProxy> beanManagers;
    // used for caller detection
    private final Set<String> subclassNames;

    public Weld() {
        beanManagers = CacheBuilder.newBuilder().weakValues().build(new ClassNameToBeanManager());
        Set<String> names = new HashSet<String>();
        for (Class<?> clazz = getClass(); clazz != CDI.class; clazz = clazz.getSuperclass()) {
            names.add(clazz.getName());
        }
        this.subclassNames = Collections.unmodifiableSet(names);
    }

    /**
     * Callback that allows to override the behavior when CDI.current() is not called from within a bean archive.
     */
    protected BeanManagerImpl unsatisfiedBeanManager(String callerClassName) {
        throw new IllegalStateException(BeanManagerMessage.UNSATISFIED_BEAN_MANAGER, callerClassName);
    }

    /**
     * Callback that allows to override the behavior when class that invoked CDI.current() is placed in multiple bean archives.
     */
    protected BeanManagerImpl ambiguousBeanManager(String callerClassName, Set<BeanManagerImpl> managers) {
        throw new IllegalStateException(BeanManagerMessage.AMBIGUOUS_BEAN_MANAGER, callerClassName);
    }

    @Override
    public BeanManagerProxy getBeanManager() {
        ContainerState state = Container.instance().getState();
        if (state.equals(ContainerState.STOPPED) || state.equals(ContainerState.SHUTDOWN)) {
            throw new IllegalStateException(BeanManagerMessage.BEAN_MANAGER_NOT_AVAILABLE);
        }
        return beanManagers.getUnchecked(getCallingClassName());
    }

    /**
     * Examines {@link StackTraceElement}s to figure out which class invoked a method on {@link CDI}.
     */
    protected String getCallingClassName() {
        boolean outerSubclassReached = false;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            // the method call that leads to the first invocation of this class or its subclass is considered the caller
            if (!subclassNames.contains(element.getClassName())) {
                if (outerSubclassReached) {
                    return element.getClassName();
                }
            } else {
                outerSubclassReached = true;
            }
        }
        throw new IllegalStateException(BeanManagerMessage.UNABLE_TO_IDENTIFY_BEAN_MANAGER);
    }

    @Override
    public Iterator<Object> iterator() {
        return getInstance().iterator();
    }

    @Override
    public Object get() {
        return getInstance().get();
    }

    @Override
    public Instance<Object> select(Annotation... qualifiers) {
        return getInstance().select(qualifiers);
    }

    @Override
    public <U> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return getInstance().select(subtype, qualifiers);
    }

    @Override
    public <U> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return getInstance().select(subtype, qualifiers);
    }

    @Override
    public boolean isUnsatisfied() {
        return getInstance().isUnsatisfied();
    }

    @Override
    public boolean isAmbiguous() {
        return getInstance().isAmbiguous();
    }

    protected Instance<Object> getInstance() {
        return getBeanManager().delegate().instance();
    }

    @Override
    public String toString() {
        return "Weld";
    }

    public void cleanup() {
        beanManagers.invalidateAll();
    }

    @Override
    public void destroy(Object instance) {
        getInstance().destroy(instance);
    }
}
