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
package org.jboss.weld.bootstrap;

import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEvents;
import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.ClassFileInfo;
import org.jboss.weld.resources.spi.ClassFileInfo.NestingType;
import org.jboss.weld.resources.spi.ClassFileInfoException;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.bytecode.BytecodeUtils;

/**
 * Specialized version of {@link AnnotatedTypeLoader}. This implementation uses {@link ClassFileServices} to avoid loading
 * application classes that are not
 * needed for Weld. In addition, this implementation feeds {@link SlimAnnotatedTypeContext} with {@link ClassFileInfo} and
 * resolved {@link ProcessAnnotatedType}
 * observer methods. If {@link ClassFileServices} is not sufficient to load an annotated type (e.g. superclass information
 * missing from the index) then a fall back
 * to {@link AnnotatedTypeLoader} is performed.
 *
 * @author Jozef Hartinger
 *
 */
class FastAnnotatedTypeLoader extends AnnotatedTypeLoader {

    private final ClassFileServices classFileServices;
    private final FastProcessAnnotatedTypeResolver resolver;
    private final AnnotatedTypeLoader fallback;

    FastAnnotatedTypeLoader(BeanManagerImpl manager, ClassTransformer transformer, ClassFileServices classFileServices,
            ContainerLifecycleEvents events, FastProcessAnnotatedTypeResolver resolver) {
        super(manager, transformer, events);
        this.fallback = new AnnotatedTypeLoader(manager, transformer, events);
        this.classFileServices = classFileServices;
        this.resolver = resolver;
    }

    @Override
    public <T> SlimAnnotatedTypeContext<T> loadAnnotatedType(String className, String bdaId) {
        try {
            final ClassFileInfo classFileInfo = classFileServices.getClassFileInfo(className);

            // firstly, check if this class is an annotation
            if ((classFileInfo.getModifiers() & BytecodeUtils.ANNOTATION) != 0) {
                // This is an annotation - an annotation may not end up as a managed bean nor be observer by PAT observer. Skip it.
                return null;
            }
            if (classFileInfo.isVetoed()) {
                return null;
            }
            if (classFileInfo.getNestingType().equals(NestingType.NESTED_LOCAL)
                    || classFileInfo.getNestingType().equals(NestingType.NESTED_ANONYMOUS)) {
                return null;
            }

            // secondly, let's resolve PAT observers for this class
            Set<ContainerLifecycleEventObserverMethod<?>> observerMethods = Collections.emptySet();
            if (containerLifecycleEvents.isProcessAnnotatedTypeObserved()) {
                observerMethods = resolver.resolveProcessAnnotatedTypeObservers(classFileServices, className);
                if (!observerMethods.isEmpty()) {
                    // there are PAT observers for this class, register the class now
                    return createContext(className, classFileInfo, observerMethods, bdaId);
                }
            }

            if (Beans.isDecoratorDeclaringInAppropriateConstructor(classFileInfo)) {
                BootstrapLogger.LOG.decoratorWithNonCdiConstructor(classFileInfo.getClassName());
            }

            // lastly, check if this class fulfills CDI managed bean requirements - if it does, add the class
            if (Beans.isTypeManagedBeanOrDecoratorOrInterceptor(classFileInfo)) {
                return createContext(className, classFileInfo, observerMethods, bdaId);
            }
            return null;
        } catch (ClassFileInfoException e) {
            BootstrapLogger.LOG.exceptionLoadingAnnotatedType(e.getMessage());
            return fallback.loadAnnotatedType(className, bdaId);
        }
    }

    private <T> SlimAnnotatedTypeContext<T> createContext(String className, ClassFileInfo classFileInfo,
            Set<ContainerLifecycleEventObserverMethod<?>> observerMethods, String bdaId) {
        final SlimAnnotatedType<T> type = loadSlimAnnotatedType(this.<T> loadClass(className), bdaId);
        if (type != null) {
            return SlimAnnotatedTypeContext.of(type, classFileInfo, observerMethods);
        }
        return null;
    }

    private <T> SlimAnnotatedType<T> loadSlimAnnotatedType(Class<T> clazz, String bdaId) {
        if (clazz != null) {
            try {
                return classTransformer.getBackedAnnotatedType(clazz, bdaId);
            } catch (ResourceLoadingException e) {
                missingDependenciesRegistry.handleResourceLoadingException(clazz.getName(), e);
            }
        }
        return null;
    }

}
