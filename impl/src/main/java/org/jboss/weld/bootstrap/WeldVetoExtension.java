/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.bootstrap;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.util.collections.ImmutableList;

/**
 *
 * @author Martin Kouba
 * @see ConfigurationKey#VETO_TYPE_WITHOUT_BEAN_DEFINING_ANNOTATION
 */
class WeldVetoExtension implements Extension {

    private Pattern vetoAnnotatedTypePattern;

    protected List<Class<? extends Annotation>> beanDefiningAnnotations;

    protected List<Class<? extends Annotation>> metaAnnotations;

    WeldVetoExtension(String regex) {
        this.vetoAnnotatedTypePattern = Pattern.compile(regex);
        this.metaAnnotations = ImmutableList.of(Stereotype.class, NormalScope.class);
        this.beanDefiningAnnotations = ImmutableList.of(
                Dependent.class, RequestScoped.class, ConversationScoped.class, SessionScoped.class, ApplicationScoped.class,
                javax.interceptor.Interceptor.class, javax.decorator.Decorator.class,
                Model.class);
    }

    void processAnnotatedType(@Observes ProcessAnnotatedType<?> event) {
        if (vetoAnnotatedTypePattern.matcher(event.getAnnotatedType().getJavaClass().getName()).matches()
                && !hasBeanDefiningAnnotation(event.getAnnotatedType())) {
            // Logging is not necessary - see BootstrapLogger.annotatedTypeVetoed(Object, Object)
            event.veto();
        }
    }

    void cleanupAfterBoot(@Observes AfterDeploymentValidation event) {
        this.vetoAnnotatedTypePattern = null;
        this.beanDefiningAnnotations = null;
        this.metaAnnotations = null;
    }

    private boolean hasBeanDefiningAnnotation(AnnotatedType<?> annotatedType) {
        for (Class<? extends Annotation> beanDefiningAnnotation : beanDefiningAnnotations) {
            if (annotatedType.isAnnotationPresent(beanDefiningAnnotation)) {
                return true;
            }
        }
        for (Class<? extends Annotation> metaAnnotation : metaAnnotations) {
            // The check is not perfomed recursively as bean defining annotations must be declared directly on a bean class
            if (hasBeanDefiningMetaAnnotationSpecified(annotatedType.getAnnotations(), metaAnnotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBeanDefiningMetaAnnotationSpecified(Set<Annotation> annotations, Class<? extends Annotation> metaAnnotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(metaAnnotationType)) {
                return true;
            }
        }
        return false;
    }

}
