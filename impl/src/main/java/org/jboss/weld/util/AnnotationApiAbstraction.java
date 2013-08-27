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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.resources.spi.ResourceLoader;

public class AnnotationApiAbstraction extends ApiAbstraction implements Service {

    public final Class<? extends Annotation> PRIORITY_ANNOTATION_CLASS;
    private final Method PRIORITY_VALUE;

    public AnnotationApiAbstraction(ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.PRIORITY_ANNOTATION_CLASS = annotationTypeForName("javax.annotation.Priority");
        Method method = null;
        if (!PRIORITY_ANNOTATION_CLASS.equals(DummyAnnotation.class)) {
            try {
                method = PRIORITY_ANNOTATION_CLASS.getMethod("value");
            } catch (Throwable e) {
                throw new WeldException(e);
            }
        }
        this.PRIORITY_VALUE = method;
    }

    public Integer getPriority(Object annotationInstance) {
        if (PRIORITY_VALUE == null) {
            return null;
        }
        try {
            return (Integer) PRIORITY_VALUE.invoke(annotationInstance);
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public void cleanup() {
    }
}
