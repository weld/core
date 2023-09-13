/*
* JBoss, Home of Professional Open Source
* Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events.configurator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.inject.Qualifier;

import org.jboss.weld.util.collections.ImmutableSet;

/**
 *
 * @author Martin Kouba
 */
class Configurators {

    private Configurators() {
    }

    static Set<Annotation> getQualifiers(Annotated annotated) {
        return annotated.getAnnotations().stream().filter((a) -> a.annotationType().isAnnotationPresent(Qualifier.class))
                .collect(ImmutableSet.collector());
    }

    static Set<Annotation> getQualifiers(AnnotatedElement annotatedElement) {
        Set<Annotation> qualifiers = new HashSet<>();
        Annotation[] annotations = annotatedElement.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                qualifiers.add(annotation);
            }
        }
        return qualifiers;
    }

    @SafeVarargs
    static Set<Parameter> getAnnotatedParameters(Method method, Class<? extends Annotation>... annotationClasses) {
        if (method.getParameterCount() == 0) {
            return Collections.emptySet();
        }
        Set<Parameter> annotatedParameters = new HashSet<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                if (parameter.isAnnotationPresent(annotationClass)) {
                    annotatedParameters.add(parameter);
                    break;
                }
            }
        }
        return annotatedParameters;
    }

}
