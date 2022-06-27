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
package org.jboss.weld.environment.deployment.discovery.jandex;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Function;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.weld.environment.deployment.WeldResourceLoader;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ClassFileInfo;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Jandex implementation of the {@link ClassFileServices}), which is a service that is a faster alternative to get info about class without a need to load it
 * with ClassLoader.
 *
 * @author Matej Briškár
 */
public class JandexClassFileServices implements ClassFileServices {

    private IndexView index;
    private ComputingCache<DotName, Set<String>> annotationClassAnnotationsCache;
    private final ClassLoader classLoader;

    private class AnnotationClassAnnotationLoader implements Function<DotName, Set<String>> {

        @Override
        public Set<String> apply(DotName name) {
            ClassInfo annotationClassInfo = index.getClassByName(name);
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            if (annotationClassInfo != null) {
                for (DotName annotationName : annotationClassInfo.annotationsMap().keySet()) {
                    builder.add(annotationName.toString());
                }
            } else {
                try {
                    Class<?> annotationClass = classLoader.loadClass(name.toString());
                    for (Annotation annotation : annotationClass.getDeclaredAnnotations()) {
                        builder.add(annotation.annotationType().getName());
                    }
                } catch (ClassNotFoundException e) {
                    throw CommonLogger.LOG.unableToLoadAnnotation(name.toString());
                }
            }
            return builder.build();
        }
    }

    public JandexClassFileServices(JandexDiscoveryStrategy strategy) {
        index = strategy.getCompositeJandexIndex();
        if (index == null) {
            throw CommonLogger.LOG.jandexIndexNotCreated(ClassFileServices.class.getSimpleName());
        }
        this.classLoader = WeldResourceLoader.getClassLoader();
        this.annotationClassAnnotationsCache = ComputingCacheBuilder.newBuilder().build(new AnnotationClassAnnotationLoader());
    }

    @Override
    public ClassFileInfo getClassFileInfo(String className) {
        return new JandexClassFileInfo(className, index, annotationClassAnnotationsCache, classLoader);
    }

    @Override
    public void cleanupAfterBoot() {
        if (annotationClassAnnotationsCache != null) {
            annotationClassAnnotationsCache.clear();
            annotationClassAnnotationsCache = null;
        }
        index = null;
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
    }

}