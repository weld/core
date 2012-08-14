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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * @author Pete Muir
 * @author Ales Justin
 * @author Jozef Hartinger
 */
public class CreationalContextImpl<T> implements CreationalContext<T>, WeldCreationalContext<T>, Serializable {

    private static final long serialVersionUID = 7375854583908262422L;

    @SuppressWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Not needed after initial creation")
    private transient Map<Contextual<?>, Object> incompleteInstances;
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
        this(contextual, null, Collections.synchronizedList(new ArrayList<ContextualInstance<?>>()), null);
    }

    private CreationalContextImpl(Contextual<T> contextual, Map<Contextual<?>, Object> incompleteInstances,
            List<ContextualInstance<?>> parentDependentInstancesStore, WeldCreationalContext<?> parentCreationalContext) {
        this.incompleteInstances = incompleteInstances;
        this.contextual = contextual;
        // this is direct ref by intention - to track dependencies hierarchy
        this.dependentInstances = Collections.synchronizedList(new ArrayList<ContextualInstance<?>>());
        this.parentDependentInstances = parentDependentInstancesStore;
        this.parentCreationalContext = parentCreationalContext;
    }

    public void push(T incompleteInstance) {
        if (incompleteInstances == null) {
            incompleteInstances = new HashMap<Contextual<?>, Object>();
        }
        incompleteInstances.put(contextual, incompleteInstance);
    }

    public <S> WeldCreationalContext<S> getCreationalContext(Contextual<S> contextual) {
        return new CreationalContextImpl<S>(contextual, incompleteInstances, dependentInstances, this);
    }

    public <S> S getIncompleteInstance(Contextual<S> bean) {
        return incompleteInstances == null ? null : Reflections.<S>cast(incompleteInstances.get(bean));
    }

    public boolean containsIncompleteInstance(Contextual<?> bean) {
        return incompleteInstances != null && incompleteInstances.containsKey(bean);
    }

    public void addDependentInstance(ContextualInstance<?> contextualInstance) {
        parentDependentInstances.add(contextualInstance);
    }

    @java.lang.SuppressWarnings({"NullableProblems"})
    public void release() {
        release(null, null);
    }

    // should not be public
    @java.lang.SuppressWarnings({"UnusedParameters"})
    public void release(Contextual<T> contextual, T instance) {
        for (ContextualInstance<?> dependentInstance : dependentInstances) {
            // do not destroy contextual again, since it's just being destroyed
            if (contextual == null || (dependentInstance.getContextual().equals(contextual) == false))
                destroy(dependentInstance);
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

    public List<ContextualInstance<?>> getDependentInstances() {
        return Collections.unmodifiableList(dependentInstances);
    }

    // Serialization
    protected Object writeReplace() throws ObjectStreamException {
        for (Iterator<ContextualInstance<?>> iterator = dependentInstances.iterator(); iterator.hasNext(); ) {
            ContextualInstance<?> instance = iterator.next();
            if (!(instance.getInstance() instanceof Serializable)) {
                /*
                 * This non-serializable instance is a dependency of a passivation capable enclosing bean. This means that:
                 *
                 * 1) The dependency was injected into a transient field, constructor or initializer injection point of the
                 * enclosing bean instance (otherwise it would not pass deployment validation) and is no longer retained by the
                 * enclosing bean instance. In that case we can safely destroy the dependent instance now.
                 *
                 * 2) Same as above but the enclosing bean instance retained a reference in a field that Weld has no control of.
                 * If that is the case and the bean class does not implement serialization properly, serialization of the bean
                 * instance is going to fail anyway so it is safe to destroy the dependent instance now.
                 *
                 * 3) Same as above but the bean class implements serialization properly (writeObject) so that it is able to
                 * reconstruct the state of the injected dependency on activation. If that's the case we would probably won't be
                 * able to destroy the dependency later on anyway since the identity of the dependent instance would change.
                 * Destroying it now may be risky in certain circumstances.
                 *
                 * @see https://issues.jboss.org/browse/WELD-1076
                 */
                destroy(instance);
                iterator.remove();
            }
        }
        return this;
    }
}
