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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;


/**
 * Helper class for working with interceptors and interceptor bindings.
 * @author Jozef Hartinger
 *
 */
public class Interceptors {

    private Interceptors() {
    }

    /**
     * Extracts a set of interceptor bindings from a collection of annotations.
     * @param beanManager
     * @param annotations
     * @return
     */
    public static Set<Annotation> filterInterceptorBindings(BeanManagerImpl beanManager, Collection<Annotation> annotations) {
        Set<Annotation> interceptorBindings = new InterceptorBindingSet(beanManager);
        for (Annotation annotation : annotations) {
            if (beanManager.isInterceptorBinding(annotation.annotationType())) {
                interceptorBindings.add(annotation);
            }
        }
        return interceptorBindings;
    }

    /**
     * Extracts a flat set of interception bindings from a given set of interceptor bindings.
     *
     * @param addTopLevelInterceptorBindings add top level interceptor bindings to the result set.
     * @param addInheritedInterceptorBindings add inherited level interceptor bindings to the result set.
     * @return
     */
    public static Set<Annotation> flattenInterceptorBindings(BeanManagerImpl beanManager, Collection<Annotation> annotations, boolean addTopLevelInterceptorBindings,
            boolean addInheritedInterceptorBindings) {
        Set<Annotation> flattenInterceptorBindings = new InterceptorBindingSet(beanManager);

        if (addTopLevelInterceptorBindings) {
            for (Annotation annotation : annotations) {
                boolean added = flattenInterceptorBindings.add(annotation);
                if (!added) {
                    throw BeanManagerLogger.LOG.duplicateInterceptorBinding(annotations);
                }
            }
        }
        if (addInheritedInterceptorBindings) {
            for (Annotation annotation : annotations) {
                addInheritedInterceptorBindings(annotation.annotationType(), beanManager.getServices().get(MetaAnnotationStore.class), flattenInterceptorBindings);
            }
        }
        return flattenInterceptorBindings;
    }

    private static void addInheritedInterceptorBindings(Class<? extends Annotation> bindingType, MetaAnnotationStore store, Set<Annotation> inheritedBindings) {
        Set<Annotation> metaBindings = store.getInterceptorBindingModel(bindingType).getInheritedInterceptionBindingTypes();
        inheritedBindings.addAll(metaBindings);
        for (Annotation metaBinding : metaBindings) {
            addInheritedInterceptorBindings(metaBinding.annotationType(), store, inheritedBindings);
        }
    }

    /**
     * Merge class-level interceptor bindings with interceptor bindings inherited from interceptor bindings and stereotypes.
     */
    public static Map<Class<? extends Annotation>, Annotation> mergeBeanInterceptorBindings(BeanManagerImpl beanManager, AnnotatedType<?> clazz, Collection<Class<? extends Annotation>> stereotypes) {
        Set<Annotation> classBindingAnnotations = flattenInterceptorBindings(beanManager, filterInterceptorBindings(beanManager, clazz.getAnnotations()), true, false);
        Set<Annotation> inheritedBindingAnnotations = new HashSet<Annotation>();
        inheritedBindingAnnotations.addAll(flattenInterceptorBindings(beanManager, filterInterceptorBindings(beanManager, clazz.getAnnotations()), false, true));
        for (Class<? extends Annotation> annotation : stereotypes) {
            inheritedBindingAnnotations.addAll(flattenInterceptorBindings(beanManager, filterInterceptorBindings(beanManager, beanManager.getStereotypeDefinition(annotation)), true, true));
        }
        try {
            return mergeBeanInterceptorBindings(beanManager, clazz, classBindingAnnotations, inheritedBindingAnnotations);
        } catch (DeploymentException e) {
            throw new DefinitionException(BeanLogger.LOG.conflictingInterceptorBindings(clazz.getJavaClass()));
        }
    }

    /**
     * Merge class-level interceptor bindings with interceptor bindings inherited from interceptor bindings and stereotypes.
     */
    public static Map<Class<? extends Annotation>, Annotation> mergeBeanInterceptorBindings(BeanManagerImpl beanManager, AnnotatedType<?> clazz, Collection<Annotation> classBindingAnnotations,
            Collection<Annotation> inheritedBindingAnnotations) {

        Map<Class<? extends Annotation>, Annotation> mergedBeanBindings = new HashMap<Class<? extends Annotation>, Annotation>();
        // conflict detection
        Map<Class<? extends Annotation>, Annotation> acceptedInheritedBindingTypes = new HashMap<Class<? extends Annotation>, Annotation>();

        // add all class-level interceptor bindings (these have precedence)
        for (Annotation bindingAnnotation : classBindingAnnotations) {
            if (mergedBeanBindings.containsKey(bindingAnnotation.annotationType())) {
                // not possible in Java, but we never know what extension-provided AnnotatedType returns
                throw new DeploymentException(BeanLogger.LOG.conflictingInterceptorBindings(clazz.getJavaClass()));
            }
            mergedBeanBindings.put(bindingAnnotation.annotationType(), bindingAnnotation);
        }
        // add inherited interceptor bindings
        for (Annotation bindingAnnotation : inheritedBindingAnnotations) {
            Class<? extends Annotation> bindingAnnotationType = bindingAnnotation.annotationType();
            // replace the previous interceptor binding with current binding

            Annotation previousValue = mergedBeanBindings.get(bindingAnnotationType);
            if (previousValue == null) {
                mergedBeanBindings.put(bindingAnnotationType, bindingAnnotation);
                acceptedInheritedBindingTypes.put(bindingAnnotationType, bindingAnnotation);
            } else {
                // check for conflicts
                if (acceptedInheritedBindingTypes.containsKey(bindingAnnotationType)
                        && !beanManager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(bindingAnnotationType)
                                .isEqual(previousValue, bindingAnnotation, true)) {
                    throw new DeploymentException(BeanLogger.LOG.conflictingInterceptorBindings(clazz.getJavaClass()));
                }
            }
        }
        return mergedBeanBindings;
    }
}
