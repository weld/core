/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.interceptor.reader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * A trivial implementation for handling methods
 *
 * @author
 */
public class ReflectiveAnnotatedMethodReader implements AnnotatedMethodReader<Method> {
    private static final ReflectiveAnnotatedMethodReader INSTANCE = new ReflectiveAnnotatedMethodReader();

    public static AnnotatedMethodReader<Method> getInstance() {
        return INSTANCE;
    }

    public Annotation getAnnotation(Class<? extends Annotation> annotationClass, Method methodReference) {
        return methodReference.getAnnotation(annotationClass);
    }

    public Method getJavaMethod(Method methodReference) {
        // this looks a bit of an excessive indirection, but it is designed to accomodate the case when the
        // method is wrapped in a more generic structure, like the CDI AnnotatedType
        return methodReference;
    }
}
