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
package org.jboss.weld.test.util.annotated;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
class TestAnnotationStore {

    private final HashMap<Class<? extends Annotation>, Annotation> annotationMap;
    private final Set<Annotation> annotationSet;

    TestAnnotationStore(HashMap<Class<? extends Annotation>, Annotation> annotationMap, Set<Annotation> annotationSet) {
        this.annotationMap = annotationMap;
        this.annotationSet = annotationSet;
    }

    TestAnnotationStore() {
        this.annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
        this.annotationSet = new HashSet<Annotation>();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return annotationType.cast(annotationMap.get(annotationType));
    }

    public Set<Annotation> getAnnotations() {
        return Collections.unmodifiableSet(annotationSet);
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return annotationMap.containsKey(annotationType);
    }

}
