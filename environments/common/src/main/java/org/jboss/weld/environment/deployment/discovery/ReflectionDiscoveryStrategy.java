/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.deployment.discovery;

import static org.jboss.weld.environment.util.Reflections.hasBeanDefiningMetaAnnotationSpecified;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.reflection.Reflections;

/**
 * This implementation supports bean-discovery-mode="annotated" and makes use of reflection to detect a class with a bean defining annotation.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class ReflectionDiscoveryStrategy extends AbstractDiscoveryStrategy {

    private final AtomicBoolean annotatedDiscoveryProcessed;

    public ReflectionDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap, Set<Class<? extends Annotation>> initialBeanDefiningAnnotations) {
        super(resourceLoader, bootstrap, initialBeanDefiningAnnotations);
        this.annotatedDiscoveryProcessed = new AtomicBoolean(false);
        registerHandler(new FileSystemBeanArchiveHandler());
    }

    @Override
    protected WeldBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        if (annotatedDiscoveryProcessed.compareAndSet(false, true)) {
            CommonLogger.LOG.reflectionFallback();
        }
        Iterator<String> classIterator = builder.getClassIterator();
        while (classIterator.hasNext()) {
            String className = classIterator.next();
            Class<?> clazz = Reflections.loadClass(className, resourceLoader);
            if (clazz == null || !hasBeanDefiningAnnotation(clazz, initialBeanDefiningAnnotations)) {
                classIterator.remove();
            }
        }
        return builder.build();
    }

    private boolean hasBeanDefiningAnnotation(Class<?> clazz, Set<Class<? extends Annotation>> initialBeanDefiningAnnotations) {
        for (Class<? extends Annotation> beanDefiningAnnotation : initialBeanDefiningAnnotations) {
            if (clazz.isAnnotationPresent(beanDefiningAnnotation)) {
                return true;
            }
        }
        for (Class<? extends Annotation> metaAnnotation : metaAnnotations) {
            // The check is not perfomed recursively as bean defining annotations must be declared directly on a bean class
            // Also we don't cache the results and rely completely on the reflection optimizations
            if (hasBeanDefiningMetaAnnotationSpecified(clazz.getAnnotations(), metaAnnotation)) {
                return true;
            }
        }
        return false;
    }

}
