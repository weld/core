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
package org.jboss.weld.environment.se.discovery;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.weld.environment.se.discovery.url.DiscoveryStrategy;
import org.jboss.weld.environment.se.discovery.url.JandexEnabledDiscoveryStrategy;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.resources.spi.ClassFileInfo;
import org.jboss.weld.resources.spi.ClassFileServices;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

/**
 * Weld SE implementation of the {@link ClassFileServices}), which is a service that is a faster alternative to get info about class without a need to load it
 * with ClassLoader.
 *
 * @author Matej Briškár
 */
public class WeldSEClassFileServices implements ClassFileServices {

    private static final String JANDEX_INDEX_IS_NULL_MESSAGE = "Jandex index is null in the constructor of class: ";
    private IndexView index;
    private LoadingCache<DotName, Set<String>> annotationClassAnnotationsCache;
    private final ClassLoader classLoader;

    private class AnnotationClassAnnotationLoader extends CacheLoader<DotName, Set<String>> {
        private static final String UNABLE_TO_LOAD_ANNOTATION_MESSAGE = "Unable to load annotation: ";

        @Override
        public Set<String> load(DotName name) throws Exception {
            ClassInfo annotationClassInfo = index.getClassByName(name);
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            if (annotationClassInfo != null) {
                for (DotName annotationName : annotationClassInfo.annotations().keySet()) {
                    builder.add(annotationName.toString());
                }
            } else {
                try {
                    Class<?> annotationClass = classLoader.loadClass(name.toString());
                    for (Annotation annotation : annotationClass.getDeclaredAnnotations()) {
                        builder.add(annotation.annotationType().getName());
                    }
                } catch (ClassNotFoundException e) {
                    throw WeldSELogger.LOG.unableToLoadAnnotation(name.toString());
                }
            }
            return builder.build();
        }
    }

    public WeldSEClassFileServices(DiscoveryStrategy strategy) {
        JandexEnabledDiscoveryStrategy jandexStrategy = (JandexEnabledDiscoveryStrategy) strategy;
        index = jandexStrategy.getCompositeJandexIndex();
        if (index == null) {
            throw new IllegalStateException(JANDEX_INDEX_IS_NULL_MESSAGE + ClassFileServices.class.getSimpleName());
        }
        this.classLoader = WeldSEResourceLoader.getClassLoader();
        this.annotationClassAnnotationsCache = CacheBuilder.newBuilder().build(new AnnotationClassAnnotationLoader());
    }

    @Override
    public ClassFileInfo getClassFileInfo(String className) {
        return new WeldSEClassFileInfo(className, index, annotationClassAnnotationsCache, classLoader);
    }

    @Override
    public void cleanupAfterBoot() {
        if (annotationClassAnnotationsCache != null) {
            annotationClassAnnotationsCache.invalidateAll();
            annotationClassAnnotationsCache = null;
        }
        index = null;
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
    }

}