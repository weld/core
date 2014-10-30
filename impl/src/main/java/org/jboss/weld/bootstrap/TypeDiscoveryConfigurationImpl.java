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
package org.jboss.weld.bootstrap;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.util.collections.ImmutableSet;

public class TypeDiscoveryConfigurationImpl implements TypeDiscoveryConfiguration {

    private final Set<Class<? extends Annotation>> beanDefiningAnnotations;

    protected TypeDiscoveryConfigurationImpl(Set<Class<? extends Annotation>> beanDefiningAnnotations) {
        this.beanDefiningAnnotations = ImmutableSet.copyOf(beanDefiningAnnotations);
    }

    @Override
    public Set<Class<? extends Annotation>> getKnownBeanDefiningAnnotations() {
        return beanDefiningAnnotations;
    }

    @Override
    public String toString() {
        return "TypeDiscoveryConfigurationImpl [beanDefiningAnnotations=" + beanDefiningAnnotations + "]";
    }
}
