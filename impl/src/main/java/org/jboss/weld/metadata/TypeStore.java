/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.metadata;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.jboss.weld.bootstrap.api.Service;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pmuir
 */
public class TypeStore implements Service {

    private final SetMultimap<Class<? extends Annotation>, Annotation> extraAnnotations;

    public TypeStore() {
        this.extraAnnotations = Multimaps.synchronizedSetMultimap(Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<Annotation>>(), new Supplier<Set<Annotation>>() {

            public Set<Annotation> get() {
                return new HashSet<Annotation>();
            }

        }));
    }

    public Set<Annotation> get(Class<? extends Annotation> annotationType) {
        return extraAnnotations.get(annotationType);
    }

    public void add(Class<? extends Annotation> annotationType, Annotation annotation) {
        this.extraAnnotations.put(annotationType, annotation);
    }

    public void addAll(Class<? extends Annotation> annotationType, Set<Annotation> annotations) {
        this.extraAnnotations.get(annotationType).addAll(annotations);
    }

    public void cleanup() {
        this.extraAnnotations.clear();
    }

}
