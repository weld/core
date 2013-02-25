/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.interceptor.util;


import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public final class InterceptionTypeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(InterceptionTypeRegistry.class);
    private static final Map<InterceptionType, Class<? extends Annotation>> INTERCEPTOR_ANNOTATION_CLASSES;

    static {
        final Map<InterceptionType, Class<? extends Annotation>> interceptionAnnotationClasses = new HashMap<InterceptionType, Class<? extends Annotation>>();

        for (InterceptionType interceptionType : InterceptionType.values()) {
            try {
                interceptionAnnotationClasses.put(interceptionType, (Class<? extends Annotation>) InterceptionTypeRegistry.class.getClassLoader().loadClass(interceptionType.annotationClassName()));
            } catch (Exception e) {
                LOG.warn("Class '" + interceptionType.annotationClassName() + "' not found, interception based on it is not enabled");
            }
        }
        INTERCEPTOR_ANNOTATION_CLASSES = Collections.unmodifiableMap(interceptionAnnotationClasses);

    }

    public static Collection<InterceptionType> getSupportedInterceptionTypes() {
        return INTERCEPTOR_ANNOTATION_CLASSES.keySet();
    }

    public static boolean isSupported(InterceptionType interceptionType) {
        return INTERCEPTOR_ANNOTATION_CLASSES.containsKey(interceptionType);
    }

    public static Class<? extends Annotation> getAnnotationClass(InterceptionType interceptionType) {
        return INTERCEPTOR_ANNOTATION_CLASSES.get(interceptionType);
    }

}
