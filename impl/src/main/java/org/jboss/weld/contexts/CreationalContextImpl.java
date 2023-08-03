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
package org.jboss.weld.contexts;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.construction.api.AroundConstructCallback;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.interceptor.proxy.InterceptionContext;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Pete Muir
 * @author Ales Justin
 * @author Jozef Hartinger
 */
public class CreationalContextImpl<T> implements CreationalContext<T>, WeldCreationalContext<T>, Serializable {

    private static final long serialVersionUID = 7375854583908262422L;

    private static final SerializationProxy SERIALIZATION_PROXY = new SerializationProxy();

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Not needed after initial creation")
    private transient Map<Contextual<?>, Object> incompleteInstances;
    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Not needed after initial creation")
    private final transient Contextual<T> contextual;

    private final List<ContextualInstance<?>> dependentInstances;

    private final List<ContextualInstance<?>> parentDependentInstances;

    private final CreationalContextImpl<?> parentCreationalContext;

    /**
     * Needs to be always initialized as a Set that managed WeakReferences to avoid cyclic references and leaks.
     * Precondition for access when non-null: synchronized (dependentInstances).
     *
     * @see #initDestroyedIfNeeded()
     */
    private transient Set<ContextualInstance<?>> destroyed;

    private transient List<ResourceReference<?>> resourceReferences;

    private transient boolean constructorInterceptionSuppressed;

    private transient List<AroundConstructCallback<T>> aroundConstructCallbacks;

    private transient InterceptionContext aroundConstructInterceptionContext;

    public CreationalContextImpl(Contextual<T> contextual) {
        this(contextual, null, Collections.synchronizedList(new ArrayList<ContextualInstance<?>>()), null);
    }

    private CreationalContextImpl(Contextual<T> contextual, Map<Contextual<?>, Object> incompleteInstances,
            List<ContextualInstance<?>> parentDependentInstancesStore, CreationalContextImpl<?> parentCreationalContext) {
        this.incompleteInstances = incompleteInstances;
        this.contextual = contextual;
        // this is direct ref by intention - to track dependencies hierarchy
        this.dependentInstances = Collections.synchronizedList(new ArrayList<ContextualInstance<?>>());
        this.parentDependentInstances = parentDependentInstancesStore;
        this.parentCreationalContext = parentCreationalContext;
        this.constructorInterceptionSuppressed = false;
    }

    private CreationalContextImpl() {
        this.contextual = null;
        this.parentCreationalContext = null;
        // We can't use immutable empty lists because of some rare scenarios with Instance.get()
        this.dependentInstances = Collections.synchronizedList(new ArrayList<ContextualInstance<?>>());
        this.parentDependentInstances = Collections.synchronizedList(new ArrayList<ContextualInstance<?>>());
    }

    public void push(T incompleteInstance) {
        if (incompleteInstances == null) {
            incompleteInstances = new HashMap<Contextual<?>, Object>();
        }
        incompleteInstances.put(contextual, incompleteInstance);
    }

    public <S> CreationalContextImpl<S> getCreationalContext(Contextual<S> contextual) {
        return new CreationalContextImpl<S>(contextual, incompleteInstances, dependentInstances, this);
    }

    public <S> CreationalContextImpl<S> getProducerReceiverCreationalContext(Contextual<S> contextual) {
        return new CreationalContextImpl<S>(contextual, incompleteInstances != null ? new HashMap<Contextual<?>, Object>(incompleteInstances) : null, Collections.synchronizedList(new ArrayList<ContextualInstance<?>>()), null);
    }

    public <S> S getIncompleteInstance(Contextual<S> bean) {
        return incompleteInstances == null ? null : Reflections.<S> cast(incompleteInstances.get(bean));
    }

    public boolean containsIncompleteInstance(Contextual<?> bean) {
        return incompleteInstances != null && incompleteInstances.containsKey(bean);
    }

    public void addDependentInstance(ContextualInstance<?> contextualInstance) {
        parentDependentInstances.add(contextualInstance);
    }

    public void release() {
        release(null, null);
    }

    // should not be public
    public void release(Contextual<T> contextual, T instance) {
        synchronized (dependentInstances) {
            for (ContextualInstance<?> dependentInstance : dependentInstances) {
                if (contextual == null || !(dependentInstance.getContextual().equals(contextual))) {
                    destroy(dependentInstance);
                } else {
                    // do not destroy contextual again, since it's just being destroyed, but make sure its dependencies
                    // are destroyed
                    release(dependentInstance);
                }
            }
        }
        if (resourceReferences != null) {
            for (ResourceReference<?> reference : resourceReferences) {
                reference.release();
            }
        }
    }

    private <T> void destroy(ContextualInstance<T> beanInstance) {
        // Precondition: synchronized (dependentInstances)
        initDestroyedIfNeeded();
        if (this.destroyed.add(beanInstance)) {
            beanInstance.getContextual().destroy(beanInstance.getInstance(), beanInstance.getCreationalContext());
        }
    }

    private <T> void release(ContextualInstance<T> beanInstance) {
        // Precondition: synchronized (dependentInstances)
        initDestroyedIfNeeded();
        if (this.destroyed.add(beanInstance)) {
            CreationalContext<T> cc = beanInstance.getCreationalContext();
            if (cc instanceof CreationalContextImpl<?>) {
                ((CreationalContextImpl<T>) cc).release(beanInstance.getContextual(), beanInstance.getInstance());
            } else {
                cc.release();
            }
        }
    }

    /**
     * @return the parent {@link CreationalContext} or null if there isn't any parent.
     */
    public CreationalContextImpl<?> getParentCreationalContext() {
        return parentCreationalContext;
    }

    /**
     * Returns an unmodifiable list of dependent instances.
     */
    public List<ContextualInstance<?>> getDependentInstances() {
        return WeldCollections.immutableListView(dependentInstances);
    }

    // Serialization
    protected Object writeReplace() throws ObjectStreamException {
        synchronized (dependentInstances) {
            for (Iterator<ContextualInstance<?>> iterator = dependentInstances.iterator(); iterator.hasNext();) {
                ContextualInstance<?> instance = iterator.next();
                if (!(instance.getInstance() instanceof Serializable)) {
                    /*
                     * This non-serializable instance is a dependency of a passivation capable enclosing bean. This means that:
                     *
                     * 1) The dependency was injected into a transient field, constructor or initializer injection point of the enclosing bean instance
                     * (otherwise it would not pass deployment validation) and is no longer retained by the enclosing bean instance. In that case we can safely
                     * destroy the dependent instance now.
                     *
                     * 2) Same as above but the enclosing bean instance retained a reference in a field that Weld has no control of. If that is the case and the
                     * bean class does not implement serialization properly, serialization of the bean instance is going to fail anyway so it is safe to destroy
                     * the dependent instance now.
                     *
                     * 3) Same as above but the bean class implements serialization properly (writeObject) so that it is able to reconstruct the state of the
                     * injected dependency on activation. If that's the case we would probably won't be able to destroy the dependency later on anyway since the
                     * identity of the dependent instance would change. Destroying it now may be risky in certain circumstances.
                     *
                     * @see https://issues.jboss.org/browse/WELD-1076
                     */
                    destroy(instance);
                    iterator.remove();
                }
            }
        }
        // Return a serialization proxy for an "empty" instance
        if (parentCreationalContext == null && dependentInstances.isEmpty() && (parentDependentInstances == null || parentDependentInstances.isEmpty())) {
            return SERIALIZATION_PROXY;
        }
        return this;
    }

    /**
     * Register a {@link ResourceReference} as a dependency. {@link ResourceReference#release()} will be called on every {@link ResourceReference} once this
     * {@link CreationalContext} instance is released.
     */
    public void addDependentResourceReference(ResourceReference<?> resourceReference) {
        if (resourceReferences == null) {
            this.resourceReferences = new ArrayList<ResourceReference<?>>();
        }
        this.resourceReferences.add(resourceReference);
    }

    /**
     * Destroys dependent instance
     *
     * @param instance
     * @return true if the instance was destroyed, false otherwise
     */
    public boolean destroyDependentInstance(T instance) {
        synchronized (dependentInstances) {
            for (Iterator<ContextualInstance<?>> iterator = dependentInstances.iterator(); iterator.hasNext();) {
                ContextualInstance<?> contextualInstance = iterator.next();
                if (contextualInstance.getInstance() == instance) {
                    iterator.remove();
                    destroy(contextualInstance);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the {@link Contextual} for which this {@link CreationalContext} is created.
     */
    public Contextual<T> getContextual() {
        return contextual;
    }

    public List<AroundConstructCallback<T>> getAroundConstructCallbacks() {
        if (aroundConstructCallbacks == null) {
            return Collections.emptyList();
        }
        return aroundConstructCallbacks;
    }

    @Override
    public void setConstructorInterceptionSuppressed(boolean value) {
        this.constructorInterceptionSuppressed = value;
    }

    @Override
    public boolean isConstructorInterceptionSuppressed() {
        return this.constructorInterceptionSuppressed;
    }

    @Override
    public void registerAroundConstructCallback(AroundConstructCallback<T> callback) {
        if (aroundConstructCallbacks == null) {
            this.aroundConstructCallbacks = new LinkedList<AroundConstructCallback<T>>();
        }
        this.aroundConstructCallbacks.add(callback);
    }

    /**
     *
     * @return the interception context used for Weld-managed AroundConstruct interceptors or <code>null</code> if no such interceptors were applied
     */
    public InterceptionContext getAroundConstructInterceptionContext() {
        return aroundConstructInterceptionContext;
    }

    public void setAroundConstructInterceptionContext(InterceptionContext aroundConstructInterceptionContext) {
        this.aroundConstructInterceptionContext = aroundConstructInterceptionContext;
    }

    /**
     *
     * @author Martin Kouba
     */
    private static class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 5261112077771498097L;

        @SuppressWarnings("rawtypes")
        private Object readResolve() throws ObjectStreamException {
            return new CreationalContextImpl();
        }

    }

    private void initDestroyedIfNeeded() {
        // we need to use WeakReference set so that we avoid cyclic references and memory leaks
        if (this.destroyed == null) {
            this.destroyed = Collections.newSetFromMap(new WeakHashMap<>());
        }
    }

}
