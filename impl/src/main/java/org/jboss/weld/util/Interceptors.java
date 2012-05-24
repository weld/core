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
package org.jboss.weld.util;

import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_INTERCEPTOR_BINDING;
import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
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
                    throw new IllegalArgumentException(DUPLICATE_INTERCEPTOR_BINDING, annotations);
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
            return mergeBeanInterceptorBindings(beanManager, classBindingAnnotations, inheritedBindingAnnotations);
        } catch (DeploymentException e) {
            throw new DefinitionException(CONFLICTING_INTERCEPTOR_BINDINGS, clazz.getJavaClass());
        }
    }

    /**
     * Merge class-level interceptor bindings with interceptor bindings inherited from interceptor bindings and stereotypes.
     */
    public static Map<Class<? extends Annotation>, Annotation> mergeBeanInterceptorBindings(BeanManagerImpl beanManager, Collection<Annotation> classBindingAnnotations,
            Collection<Annotation> inheritedBindingAnnotations) {

        Map<Class<? extends Annotation>, Annotation> mergedBeanBindings = new HashMap<Class<? extends Annotation>, Annotation>();
        // conflict detection
        Map<Class<? extends Annotation>, Annotation> acceptedInheritedBindingTypes = new HashMap<Class<? extends Annotation>, Annotation>();

        // add all class-level interceptor bindings (these have precedence)
        for (Annotation bindingAnnotation : classBindingAnnotations) {
            if (mergedBeanBindings.containsKey(bindingAnnotation.annotationType())) {
                // not possible in Java, but we never know what extension-provided AnnotatedType returns
                throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS);
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
                    throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS);
                }
            }
        }
        return mergedBeanBindings;
    }
}
