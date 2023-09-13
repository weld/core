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
import java.lang.annotation.Repeatable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.interceptor.InterceptorBinding;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.collections.Multimap;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * Helper class for working with interceptors and interceptor bindings.
 *
 * @author Jozef Hartinger
 *
 */
public class Interceptors {

    private Interceptors() {
    }

    /**
     * Extracts a set of interceptor bindings from a collection of annotations.
     *
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
    public static Set<Annotation> flattenInterceptorBindings(EnhancedAnnotatedType<?> clazz, BeanManagerImpl beanManager,
            Collection<Annotation> annotations, boolean addTopLevelInterceptorBindings,
            boolean addInheritedInterceptorBindings) {
        Set<Annotation> flattenInterceptorBindings = new InterceptorBindingSet(beanManager);
        MetaAnnotationStore metaAnnotationStore = beanManager.getServices().get(MetaAnnotationStore.class);

        if (addTopLevelInterceptorBindings) {
            addInterceptorBindings(clazz, annotations, flattenInterceptorBindings, metaAnnotationStore);
        }
        if (addInheritedInterceptorBindings) {
            for (Annotation annotation : annotations) {
                addInheritedInterceptorBindings(clazz, annotation.annotationType(), metaAnnotationStore,
                        flattenInterceptorBindings);
            }
        }
        return flattenInterceptorBindings;
    }

    private static void addInheritedInterceptorBindings(EnhancedAnnotatedType<?> clazz, Class<? extends Annotation> bindingType,
            MetaAnnotationStore metaAnnotationStore, Set<Annotation> flattenInterceptorBindings) {
        Set<Annotation> metaBindings = metaAnnotationStore.getInterceptorBindingModel(bindingType)
                .getInheritedInterceptionBindingTypes();
        addInterceptorBindings(clazz, metaBindings, flattenInterceptorBindings, metaAnnotationStore);
        for (Annotation metaBinding : metaBindings) {
            addInheritedInterceptorBindings(clazz, metaBinding.annotationType(), metaAnnotationStore,
                    flattenInterceptorBindings);
        }
    }

    private static void addInterceptorBindings(EnhancedAnnotatedType<?> clazz, Collection<Annotation> interceptorBindings,
            Set<Annotation> flattenInterceptorBindings,
            MetaAnnotationStore metaAnnotationStore) {
        for (Annotation annotation : interceptorBindings) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (!annotation.annotationType().isAnnotationPresent(Repeatable.class)) {
                for (Annotation binding : flattenInterceptorBindings) {
                    if (binding.annotationType().equals(annotationType)
                            && !metaAnnotationStore.getInterceptorBindingModel(annotationType).isEqual(annotation, binding,
                                    false)) {
                        if (clazz != null) {
                            throw new DefinitionException(BeanLogger.LOG.conflictingInterceptorBindings(clazz));
                        } else {
                            throw BeanManagerLogger.LOG.duplicateInterceptorBinding(annotation);
                        }

                    }
                }
            }
            flattenInterceptorBindings.add(annotation);
        }
    }

    /**
     * Merge class-level interceptor bindings with interceptor bindings inherited from interceptor bindings and stereotypes.
     */
    public static Multimap<Class<? extends Annotation>, Annotation> mergeBeanInterceptorBindings(BeanManagerImpl beanManager,
            EnhancedAnnotatedType<?> clazz, Collection<Class<? extends Annotation>> stereotypes) {
        Set<Annotation> rawBindings = clazz.getMetaAnnotations(InterceptorBinding.class);
        Set<Annotation> classBindingAnnotations = flattenInterceptorBindings(clazz, beanManager,
                filterInterceptorBindings(beanManager, rawBindings), true, false);
        Set<Annotation> inheritedBindingAnnotations = new HashSet<Annotation>();
        inheritedBindingAnnotations.addAll(flattenInterceptorBindings(clazz, beanManager,
                filterInterceptorBindings(beanManager, rawBindings), false, true));
        for (Class<? extends Annotation> annotation : stereotypes) {
            inheritedBindingAnnotations.addAll(flattenInterceptorBindings(clazz, beanManager,
                    filterInterceptorBindings(beanManager, beanManager.getStereotypeDefinition(annotation)), true, true));
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
    public static Multimap<Class<? extends Annotation>, Annotation> mergeBeanInterceptorBindings(BeanManagerImpl beanManager,
            AnnotatedType<?> clazz,
            Collection<Annotation> classBindingAnnotations, Collection<Annotation> inheritedBindingAnnotations) {

        SetMultimap<Class<? extends Annotation>, Annotation> mergedBeanBindings = SetMultimap.newSetMultimap();
        SetMultimap<Class<? extends Annotation>, Annotation> acceptedInheritedBindings = SetMultimap.newSetMultimap();
        MetaAnnotationStore metaAnnotationStore = beanManager.getServices().get(MetaAnnotationStore.class);

        // add all class-level interceptor bindings (these have precedence)
        for (Annotation binding : classBindingAnnotations) {
            Class<? extends Annotation> annotationType = binding.annotationType();
            if (!annotationType.isAnnotationPresent(Repeatable.class)) {
                // Detec conflicts for non repeating bindings
                for (Annotation mergedBinding : mergedBeanBindings.get(annotationType)) {
                    if (!metaAnnotationStore.getInterceptorBindingModel(annotationType).isEqual(binding, mergedBinding,
                            false)) {
                        throw new DeploymentException(BeanLogger.LOG.conflictingInterceptorBindings(clazz.getJavaClass()));
                    }
                }
            }
            mergedBeanBindings.put(binding.annotationType(), binding);
        }
        // add inherited interceptor bindings
        for (Annotation binding : inheritedBindingAnnotations) {
            Class<? extends Annotation> annotationType = binding.annotationType();

            if (!mergedBeanBindings.containsKey(annotationType) || annotationType.isAnnotationPresent(Repeatable.class)) {
                mergedBeanBindings.put(annotationType, binding);
                acceptedInheritedBindings.put(annotationType, binding);
            } else {
                Set<Annotation> inheritedBindings = acceptedInheritedBindings.get(annotationType);
                for (Annotation inheritedBinding : inheritedBindings) {
                    if (!metaAnnotationStore.getInterceptorBindingModel(annotationType).isEqual(binding, inheritedBinding,
                            false)) {
                        throw new DeploymentException(BeanLogger.LOG.conflictingInterceptorBindings(clazz.getJavaClass()));
                    }
                }
            }
        }
        return mergedBeanBindings;
    }
}
