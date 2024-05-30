/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
import java.lang.reflect.Method;

import org.jboss.weld.util.reflection.Reflections;

/**
 * Utility methods for working with annotations.
 *
 * @author Jozef Hartinger
 *
 */
public class Annotations {

    public static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    private static final String VALUE_MEMBER_NAME = "value";

    private Annotations() {
    }

    /**
     * Returns the value {@link Method} of a repeatable annotation container or null if the given annotation is not a repeatable
     * annotation container.
     *
     * @param annotation the given annotation
     * @return the value {@link Method} of a repeatable annotation container or null if the given annotation is not a repeatable
     *         annotation container
     */
    public static Method getRepeatableAnnotationAccessor(Class<? extends Annotation> annotation) {
        Method value = Reflections.findDeclaredMethodByName(annotation, VALUE_MEMBER_NAME);
        if (value == null) {
            return null;
        }
        if (!value.getReturnType().isArray()) {
            return null;
        }
        Repeatable repeatable = value.getReturnType().getComponentType().getAnnotation(Repeatable.class);
        if (repeatable == null) {
            return null;
        }
        if (!repeatable.value().equals(annotation)) {
            return null;
        }
        return value;
    }
}
