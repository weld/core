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
package org.jboss.weld.interceptor.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.Annotated;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ApiAbstraction;

public class InterceptorsApiAbstraction extends ApiAbstraction implements Service {

    private final Class<? extends Annotation> INTERCEPTORS_ANNOTATION_CLASS;
    private final Method interceptorsValueMethod;

    private final Class<? extends Annotation> EXCLUDE_CLASS_INTERCEPTORS_ANNOTATION_CLASS;

    public InterceptorsApiAbstraction(ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.INTERCEPTORS_ANNOTATION_CLASS = annotationTypeForName("jakarta.interceptor.Interceptors");
        this.EXCLUDE_CLASS_INTERCEPTORS_ANNOTATION_CLASS = annotationTypeForName(
                "jakarta.interceptor.ExcludeClassInterceptors");
        if (DummyAnnotation.class.isAssignableFrom(INTERCEPTORS_ANNOTATION_CLASS)) {
            this.interceptorsValueMethod = null;
        } else {
            try {
                this.interceptorsValueMethod = INTERCEPTORS_ANNOTATION_CLASS.getMethod("value");
            } catch (Exception e) {
                throw UtilLogger.LOG.annotationValuesInaccessible(e);
            }
        }
    }

    public Class<? extends Annotation> getInterceptorsAnnotationClass() {
        return INTERCEPTORS_ANNOTATION_CLASS;
    }

    public Class<? extends Annotation> getExcludeClassInterceptorsAnnotationClass() {
        return EXCLUDE_CLASS_INTERCEPTORS_ANNOTATION_CLASS;
    }

    public Class<?>[] extractInterceptorClasses(Annotated annotated) {
        Annotation annotation = annotated.getAnnotation(INTERCEPTORS_ANNOTATION_CLASS);
        if (annotation != null) {
            try {
                return (Class<?>[]) interceptorsValueMethod.invoke(annotation);
            } catch (Exception e) {
                throw UtilLogger.LOG.annotationValuesInaccessible(e);
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
    }

}
