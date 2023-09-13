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

import static org.jboss.weld.util.reflection.Reflections.cast;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEvents;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.Beans;

/**
 * Takes care of loading a class, creating {@link BackedAnnotatedType} and creating {@link SlimAnnotatedTypeContext}.
 *
 * @author Jozef Hartinger
 *
 */
class AnnotatedTypeLoader {

    final ResourceLoader resourceLoader;
    final ClassTransformer classTransformer;
    final MissingDependenciesRegistry missingDependenciesRegistry;
    final ContainerLifecycleEvents containerLifecycleEvents;

    static final String MODULEINFO_CLASS_NAME = "module-info";
    static final String MULTI_RELEASE_JAR_PATH = "META-INF.versions";

    AnnotatedTypeLoader(BeanManagerImpl manager, ClassTransformer transformer,
            ContainerLifecycleEvents containerLifecycleEvents) {
        this.resourceLoader = manager.getServices().get(ResourceLoader.class);
        this.classTransformer = transformer;
        this.missingDependenciesRegistry = manager.getServices().get(MissingDependenciesRegistry.class);
        this.containerLifecycleEvents = containerLifecycleEvents;
    }

    /**
     * Creates a new {@link SlimAnnotatedTypeContext} instance for a class with the specified class name. This method may return
     * null if there is a problem
     * loading the class or this class is not needed for further processing (e.g. an annotation or a vetoed class).
     *
     * @param className the specified class name
     * @param bdaId the identifier of the bean archive this class resides in
     * @return a new {@code SlimAnnotatedTypeContext} for a specified class or null
     */
    public <T> SlimAnnotatedTypeContext<T> loadAnnotatedType(String className, String bdaId) {
        return loadAnnotatedType(this.<T> loadClass(className), bdaId);
    }

    /**
     * Creates a new {@link SlimAnnotatedTypeContext} instance for the given class. This method may return null if there is a
     * problem
     * loading the class or this class is not needed for further processing (e.g. an annotation or a vetoed class).
     *
     * @param clazz the given class
     * @param bdaId the identifier of the bean archive this class resides in
     * @return a new {@code SlimAnnotatedTypeContext} for a specified class or null
     */
    public <T> SlimAnnotatedTypeContext<T> loadAnnotatedType(Class<T> clazz, String bdaId) {
        return createContext(internalLoadAnnotatedType(clazz, bdaId));
    }

    protected <T> Class<T> loadClass(String className) {
        if (isModuleInfo(className) || isMultiReleaseJarClass(className)) {
            return null;
        }
        try {
            return cast(resourceLoader.classForName(className));
        } catch (ResourceLoadingException e) {
            missingDependenciesRegistry.handleResourceLoadingException(className, e);
            return null;
        }
    }

    protected <T> SlimAnnotatedType<T> internalLoadAnnotatedType(Class<T> clazz, String bdaId) {
        // do not load if clazz is null, an annotation or anonymous/local class
        if (clazz != null && !clazz.isAnnotation() && !clazz.isAnonymousClass() && !clazz.isLocalClass()) {
            try {
                if (!Beans.isVetoed(clazz)) { // may throw ArrayStoreException - see bug http://bugs.sun.com/view_bug.do?bug_id=7183985
                    containerLifecycleEvents.preloadProcessAnnotatedType(clazz);
                    try {
                        return classTransformer.getBackedAnnotatedType(clazz, bdaId);
                    } catch (ResourceLoadingException e) {
                        missingDependenciesRegistry.handleResourceLoadingException(clazz.getName(), e);
                    }
                }
            } catch (ArrayStoreException e) {
                missingDependenciesRegistry.handleResourceLoadingException(clazz.getName(), e);
            }
        }
        return null;
    }

    protected <T> SlimAnnotatedTypeContext<T> createContext(SlimAnnotatedType<T> type) {
        if (type != null) {
            return SlimAnnotatedTypeContext.of(type);
        }
        return null;
    }

    protected boolean isModuleInfo(String className) {
        return MODULEINFO_CLASS_NAME.equals(className);
    }

    /**
     * Determines if the class is a MR JAR class, e.g. checks if its path starts with "META-INF.versions"
     *
     * @param className name of the class
     */
    protected boolean isMultiReleaseJarClass(String className) {
        return className.startsWith(MULTI_RELEASE_JAR_PATH);
    }
}
