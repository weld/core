/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resources;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;

import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class DefaultReflectionCache extends AbstractBootstrapService implements ReflectionCache {

    protected Annotation[] internalGetAnnotations(AnnotatedElement element) {
        return element.getAnnotations();
    }

    protected Annotation[] internalGetDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    private final Map<AnnotatedElement, Annotation[]> annotations;
    private final Map<AnnotatedElement, Annotation[]> declaredAnnotations;

    public DefaultReflectionCache() {
        MapMaker maker = new MapMaker();
        this.annotations = maker.makeComputingMap(new Function<AnnotatedElement, Annotation[]>() {
            @Override
            public Annotation[] apply(AnnotatedElement input) {
                return internalGetAnnotations(input);
            }
        });
        this.declaredAnnotations = maker.makeComputingMap(new Function<AnnotatedElement, Annotation[]>() {
            @Override
            public Annotation[] apply(AnnotatedElement input) {
                return internalGetDeclaredAnnotations(input);
            }
        });
    }

    public Annotation[] getAnnotations(AnnotatedElement element) {
        return annotations.get(element);
    }

    public Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return declaredAnnotations.get(element);
    }

    @Override
    public void cleanupAfterBoot() {
        annotations.clear();
        declaredAnnotations.clear();
    }
}
