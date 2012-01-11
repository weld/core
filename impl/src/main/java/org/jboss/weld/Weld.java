/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.messages.BeanManagerMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Provides convenient way to access the CDI container.
 *
 * @author Jozef Hartinger
 *
 */
public class Weld<T> extends CDI<T> {

    private class ClassNameToBeanManager implements Function<String, BeanManagerImpl> {

        /**
         * Determines the correct {@link BeanManagerImpl} based on a class name of the caller.
         */
        @Override
        public BeanManagerImpl apply(String callerClassName) {
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

    private final ConcurrentMap<String, BeanManagerImpl> beanManagers;
    // used for caller detection
    private final Set<String> subclassNames;

    public Weld() {
        beanManagers = new MapMaker().makeComputingMap(new ClassNameToBeanManager());
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
    public BeanManagerImpl getBeanManager() {
        ContainerState state = Container.instance().getState();
        if (state.equals(ContainerState.STOPPED) || state.equals(ContainerState.SHUTDOWN)) {
            throw new IllegalStateException(BeanManagerMessage.BEAN_MANAGER_NOT_AVAILABLE);
        }
        return beanManagers.get(getCallingClassName());
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
    public Iterator<T> iterator() {
        return getInstance().iterator();
    }

    @Override
    public T get() {
        return getInstance().get();
    }

    @Override
    public Instance<T> select(Annotation... qualifiers) {
        return getInstance().select(qualifiers);
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return getInstance().select(subtype, qualifiers);
    }

    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
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

    protected Instance<T> getInstance() {
        return Reflections.cast(getBeanManager().instance());
    }

    @Override
    public String toString() {
        return "Weld";
    }

    public void cleanup() {
        beanManagers.clear();
    }
}
