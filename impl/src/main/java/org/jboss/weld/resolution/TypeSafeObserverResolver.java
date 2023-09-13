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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeEventResolvable;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.event.ResolvedObservers;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 * @author Jozef Hartinger
 */
public class TypeSafeObserverResolver
        extends TypeSafeResolver<Resolvable, ObserverMethod<?>, List<ObserverMethod<?>>, ResolvedObservers<?>> {

    private static class ObserverMethodComparator implements Comparator<ObserverMethod<?>>, Serializable {

        private static final long serialVersionUID = 1L;
        private static ObserverMethodComparator INSTANCE = new ObserverMethodComparator();

        @Override
        public int compare(ObserverMethod<?> o1, ObserverMethod<?> o2) {
            ObserverMethod<?> eom1 = (ObserverMethod<?>) o1;
            ObserverMethod<?> eom2 = (ObserverMethod<?>) o2;
            return eom1.getPriority() - eom2.getPriority();
        }
    }

    private final MetaAnnotationStore metaAnnotationStore;
    private final AssignabilityRules rules;

    public TypeSafeObserverResolver(MetaAnnotationStore metaAnnotationStore, Iterable<ObserverMethod<?>> observers,
            WeldConfiguration configuration) {
        super(observers, configuration);
        this.metaAnnotationStore = metaAnnotationStore;
        this.rules = EventTypeAssignabilityRules.instance();
    }

    @Override
    protected boolean matches(Resolvable resolvable, ObserverMethod<?> observer) {
        if (!rules.matches(observer.getObservedType(), resolvable.getTypes())) {
            return false;
        }
        if (!Beans.containsAllQualifiers(QualifierInstance.of(observer.getObservedQualifiers(), metaAnnotationStore),
                resolvable.getQualifiers())) {
            return false;
        }
        if (observer instanceof ContainerLifecycleEventObserverMethod) {
            ContainerLifecycleEventObserverMethod<?> lifecycleObserver = (ContainerLifecycleEventObserverMethod<?>) observer;
            if (resolvable instanceof ProcessAnnotatedTypeEventResolvable
                    && !lifecycleObserver.getRequiredAnnotations().isEmpty()) {
                // this is a ProcessAnnotatedType observer method with @WithAnnotations and a resolvable for ProcessAnnotatedType
                ProcessAnnotatedTypeEventResolvable patResolvable = (ProcessAnnotatedTypeEventResolvable) resolvable;
                return patResolvable.containsRequiredAnnotations(lifecycleObserver.getRequiredAnnotations());
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
    protected List<ObserverMethod<?>> sortResult(Set<ObserverMethod<?>> matched) {
        List<ObserverMethod<?>> observers = new ArrayList<>(matched);
        Collections.sort(observers, ObserverMethodComparator.INSTANCE);
        return observers;
    }

    @Override
    protected ResolvedObservers<?> makeResultImmutable(List<ObserverMethod<?>> result) {
        return ResolvedObservers.of(cast(result));
    }

    public MetaAnnotationStore getMetaAnnotationStore() {
        return metaAnnotationStore;
    }
}
