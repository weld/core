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

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.context.NormalScope;
import javax.inject.Scope;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * This class is thread-safe.
 *
 * @author pmuir
 * @author Jozef Hartinger
 */
public class TypeStore implements Service {

    private final SetMultimap<Class<? extends Annotation>, Annotation> extraAnnotations;
    private final Set<Class<? extends Annotation>> extraScopes;

    public TypeStore() {
        this.extraAnnotations = SetMultimap.newConcurrentSetMultimap(CopyOnWriteArraySet::new);
        this.extraScopes = new CopyOnWriteArraySet<>();
    }

    public Set<Annotation> get(Class<? extends Annotation> annotationType) {
        return extraAnnotations.get(annotationType);
    }

    public void add(Class<? extends Annotation> annotationType, Annotation annotation) {
        if (annotation.annotationType().equals(Scope.class) || annotation.annotationType().equals(NormalScope.class)) {
            this.extraScopes.add(annotationType);
        }
        this.extraAnnotations.put(annotationType, annotation);
    }

    public boolean isExtraScope(Class<? extends Annotation> annotation) {
        return extraScopes.contains(annotation);
    }

    public void cleanup() {
        this.extraAnnotations.clear();
    }

}
