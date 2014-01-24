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
package org.jboss.weld.resolution;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeEventResolvable;
import org.jboss.weld.event.ExtensionObserverMethodImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 * @author Jozef Hartinger
 */
public class TypeSafeObserverResolver extends TypeSafeResolver<Resolvable, ObserverMethod<?>, Set<ObserverMethod<?>>> {

    private final MetaAnnotationStore metaAnnotationStore;
    private final AssignabilityRules rules;
    private final SharedObjectCache sharedObjectCache;

    public TypeSafeObserverResolver(MetaAnnotationStore metaAnnotationStore, SharedObjectCache cache, Iterable<ObserverMethod<?>> observers) {
        super(observers);
        this.metaAnnotationStore = metaAnnotationStore;
        this.rules = EventTypeAssignabilityRules.instance();
        this.sharedObjectCache = cache;
    }

    @Override
    protected boolean matches(Resolvable resolvable, ObserverMethod<?> observer) {
        if (!rules.matches(observer.getObservedType(), resolvable.getTypes())) {
            return false;
        }
        if (!Beans.containsAllQualifiers(QualifierInstance.qualifiers(metaAnnotationStore, sharedObjectCache, observer.getObservedQualifiers()), resolvable.getQualifiers())) {
            return false;
        }
        if (observer instanceof ExtensionObserverMethodImpl<?, ?>) {
            ExtensionObserverMethodImpl<?, ?> extensionObserver = (ExtensionObserverMethodImpl<?, ?>) observer;
            if (resolvable instanceof ProcessAnnotatedTypeEventResolvable && !extensionObserver.getRequiredAnnotations().isEmpty()) {
                // this is a ProcessAnnotatedType observer method with @WithAnnotations and a resolvable for ProcessAnnotatedType
                ProcessAnnotatedTypeEventResolvable patResolvable = (ProcessAnnotatedTypeEventResolvable) resolvable;
                return patResolvable.containsRequiredAnnotations(extensionObserver.getRequiredAnnotations());
            }
        } else {
            return !isContainerLifecycleEvent(resolvable); // container lifecycle events are only delivered to extensions
        }
        return true;
    }

    protected boolean isContainerLifecycleEvent(Resolvable resolvable) {
        for (Type type : resolvable.getTypes()) {
            if (Observers.CONTAINER_LIFECYCLE_EVENT_TYPES.contains(Reflections.getRawType(type))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Set<ObserverMethod<?>> filterResult(Set<ObserverMethod<?>> matched) {
        return matched;
    }

    @Override
    protected Set<ObserverMethod<?>> sortResult(Set<ObserverMethod<?>> matched) {
        return matched;
    }

    public MetaAnnotationStore getMetaAnnotationStore() {
        return metaAnnotationStore;
    }
}
