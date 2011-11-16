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
package org.jboss.weld.context;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreationalContextImpl<T> implements CreationalContext<T>, WeldCreationalContext<T>, Serializable {

    private static final long serialVersionUID = 7375854583908262422L;

    @SuppressWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Not needed after initial creation")
    private final transient Map<Contextual<?>, Object> incompleteInstances;
    @SuppressWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Not needed after initial creation")
    private final transient Contextual<T> contextual;

    private final List<ContextualInstance<?>> dependentInstances;

    private final List<ContextualInstance<?>> parentDependentInstances;

    private final WeldCreationalContext<?> parentCreationalContext;

    /*
     * A disposer method may define a metadata injection point. If that's the case, we need to preserve the metadata associated
     * with a given instance.
     */
    private Contextual<T> persistentContextual;
    private InjectionPoint persistentInjectionPoint;

    public CreationalContextImpl(Contextual<T> contextual) {
        this(contextual, new HashMap<Contextual<?>, Object>(), Collections.synchronizedList(new ArrayList<ContextualInstance<?>>()), null);
    }

    private CreationalContextImpl(Contextual<T> contextual, Map<Contextual<?>, Object> incompleteInstances,
            List<ContextualInstance<?>> parentDependentInstancesStore, WeldCreationalContext<?> parentCreationalContext) {
        this.incompleteInstances = incompleteInstances;
        this.contextual = contextual;
        this.dependentInstances = Collections.synchronizedList(new ArrayList<ContextualInstance<?>>());
        this.parentDependentInstances = parentDependentInstancesStore;
        this.parentCreationalContext = parentCreationalContext;
    }

    public void push(T incompleteInstance) {
        incompleteInstances.put(contextual, incompleteInstance);
    }

    public <S> WeldCreationalContext<S> getCreationalContext(Contextual<S> contextual) {
        return new CreationalContextImpl<S>(contextual, incompleteInstances == null ? new HashMap<Contextual<?>, Object>() : new HashMap<Contextual<?>, Object>(incompleteInstances), dependentInstances, this);
    }

    public <S> S getIncompleteInstance(Contextual<S> bean) {
        return incompleteInstances == null ? null : Reflections.<S>cast(incompleteInstances.get(bean));
    }

    public boolean containsIncompleteInstance(Contextual<?> bean) {
        return incompleteInstances == null ? false : incompleteInstances.containsKey(bean);
    }

    public void addDependentInstance(ContextualInstance<?> contextualInstance) {
        parentDependentInstances.add(contextualInstance);
    }

    public void release() {
        for (ContextualInstance<?> dependentInstance : dependentInstances) {
            destroy(dependentInstance);
        }
        if (incompleteInstances != null) {
            incompleteInstances.clear();
        }
    }

    private static <T> void destroy(ContextualInstance<T> beanInstance) {
        beanInstance.getContextual().destroy(beanInstance.getInstance(), beanInstance.getCreationalContext());
    }

    public WeldCreationalContext<?> getParentCreationalContext() {
        return parentCreationalContext;
    }

    public Contextual<T> getContextual() {
        if (persistentContextual != null) {
            return persistentContextual;
        }
        return contextual;
    }

    public void storeContextual() {
        persistentContextual = contextual;
    }

    public InjectionPoint loadInjectionPoint() {
        return persistentInjectionPoint;
    }

    public void storeInjectionPoint(InjectionPoint ip) {
        persistentInjectionPoint = ip;
    }
}
