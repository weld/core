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
package org.jboss.weld.bean.builtin;

import static org.jboss.weld.util.Preconditions.checkNotNull;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.proxy.ProxyMethodHandler;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeBeanResolver;
import org.jboss.weld.util.AnnotationApiAbstraction;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Helper implementation for Instance for getting instances
 *
 * @param <T>
 * @author Gavin King
 */
@SuppressFBWarnings(value = { "SE_NO_SUITABLE_CONSTRUCTOR", "SE_BAD_FIELD" }, justification = "Uses SerializationProxy")
public class InstanceImpl<T> extends AbstractFacade<T, Instance<T>> implements WeldInstance<T>, Serializable {

    private static final long serialVersionUID = -376721889693284887L;

    private final transient Set<Bean<?>> allBeans;
    private final transient Bean<?> bean;

    private final transient CurrentInjectionPoint currentInjectionPoint;
    private final transient InjectionPoint ip;
    private final transient EjbSupport ejbSupport;

    public static <I> Instance<I> of(InjectionPoint injectionPoint, CreationalContext<I> creationalContext,
            BeanManagerImpl beanManager) {
        return new InstanceImpl<I>(injectionPoint, creationalContext, beanManager);
    }

    private InstanceImpl(InjectionPoint injectionPoint, CreationalContext<? super T> creationalContext,
            BeanManagerImpl beanManager) {
        super(injectionPoint, creationalContext, beanManager);

        if (injectionPoint.getQualifiers().isEmpty() && Object.class.equals(getType())) {
            // Do not prefetch the beans for Instance<Object> with no qualifiers
            allBeans = null;
            bean = null;
        } else {
            this.allBeans = resolveBeans();
            // Optimization for the most common path - non-null bean means we are not unsatisfied not ambiguous
            if (allBeans.size() == 1) {
                this.bean = allBeans.iterator().next();
            } else {
                this.bean = null;
            }
        }

        this.currentInjectionPoint = beanManager.getServices().getRequired(CurrentInjectionPoint.class);
        // Generate a correct injection point for the bean, we do this by taking the original injection point and adjusting the
        // qualifiers and type
        this.ip = new DynamicLookupInjectionPoint(getInjectionPoint(), getType(), getQualifiers());
        this.ejbSupport = beanManager.getServices().get(EjbSupport.class);
    }

    public T get() {
        checkBeanResolved();
        return getBeanInstance(bean);
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotations(getQualifiers()) + " Instance<" + Formats.formatType(getType()) + ">";
    }

    public Iterator<T> iterator() {
        return new InstanceImplIterator(allBeans());
    }

    public boolean isAmbiguous() {
        return allBeans().size() > 1;
    }

    public boolean isUnsatisfied() {
        return allBeans().isEmpty();
    }

    public WeldInstance<T> select(Annotation... qualifiers) {
        return selectInstance(this.getType(), qualifiers);
    }

    public <U extends T> WeldInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return selectInstance(subtype, qualifiers);
    }

    public <U extends T> WeldInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return selectInstance(subtype.getType(), qualifiers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> WeldInstance<X> select(Type subtype, Annotation... qualifiers) {
        // verify if this was invoked on WeldInstance<Object>
        if (!this.getType().equals(Object.class)) {
            throw BeanLogger.LOG.selectByTypeOnlyWorksOnObject();
        }
        // This cast should be safe, we make sure that this method is only invoked on WeldInstance<Object>
        // and any type X will always extend Object
        return (WeldInstance<X>) selectInstance(subtype, qualifiers);
    }

    private <U extends T> WeldInstance<U> selectInstance(Type subtype, Annotation[] newQualifiers) {
        InjectionPoint modifiedInjectionPoint = new FacadeInjectionPoint(getBeanManager(), getInjectionPoint(), Instance.class,
                subtype, getQualifiers(),
                newQualifiers);
        return new InstanceImpl<U>(modifiedInjectionPoint, getCreationalContext(), getBeanManager());
    }

    @Override
    public void destroy(T instance) {
        checkNotNull(instance);
        // Attempt to destroy instance which is either a client proxy or a dependent session bean proxy
        if (instance instanceof ProxyObject) {
            ProxyObject proxy = (ProxyObject) instance;
            if (proxy.weld_getHandler() instanceof ProxyMethodHandler) {
                ProxyMethodHandler handler = (ProxyMethodHandler) proxy.weld_getHandler();
                Bean<?> bean = handler.getBean();
                if (isSessionBeanProxy(instance) && Dependent.class.equals(bean.getScope())) {
                    // Destroy internal reference to a dependent session bean
                    destroyDependentInstance(instance);
                    return;
                } else {
                    // Destroy contextual instance of a normal-scoped bean
                    Context context = getBeanManager().getContext(bean.getScope());
                    if (context instanceof AlterableContext) {
                        AlterableContext alterableContext = (AlterableContext) context;
                        alterableContext.destroy(bean);
                        return;
                    } else {
                        throw BeanLogger.LOG.destroyUnsupported(context);
                    }
                }
            }
        }
        // Attempt to destroy dependent instance which is neither a client proxy nor a dependent session bean proxy
        destroyDependentInstance(instance);
    }

    @Override
    public Handle<T> getHandle() {
        checkBeanResolved();
        return new HandlerImpl<T>(() -> getBeanInstance(bean), this, bean);
    }

    @Override
    public Handler<T> getHandler() {
        return Reflections.cast(getHandle());
    }

    @Override
    public boolean isResolvable() {
        return allBeans().size() == 1;
    }

    @Override
    public Iterable<Handle<T>> handles() {
        return () -> new HandleIterator(allBeans());
    }

    @Override
    public Iterable<Handler<T>> handlers() {
        return Reflections.cast(handles());
    }

    @Override
    public Comparator<Handler<?>> getPriorityComparator() {
        return Reflections.cast(getHandlePriorityComparator());
    }

    @Override
    public Comparator<Handle<?>> getHandlePriorityComparator() {
        return new PriorityComparator(getBeanManager().getServices().get(AnnotationApiAbstraction.class));
    }

    private boolean isSessionBeanProxy(T instance) {
        return ejbSupport != null ? ejbSupport.isSessionBeanProxy(instance) : false;
    }

    private void destroyDependentInstance(T instance) {
        CreationalContext<? super T> ctx = getCreationalContext();
        if (ctx instanceof WeldCreationalContext<?>) {
            WeldCreationalContext<? super T> weldCtx = cast(ctx);
            weldCtx.destroyDependentInstance(instance);
        }
    }

    private void checkBeanResolved() {
        if (bean != null) {
            return;
        } else if (isUnsatisfied()) {
            throw BeanManagerLogger.LOG.injectionPointHasUnsatisfiedDependencies(Formats.formatAnnotations(ip.getQualifiers()),
                    Formats.formatInjectionPointType(ip.getType()),
                    InjectionPoints.getUnsatisfiedDependenciesAdditionalInfo(ip, getBeanManager()));
        } else {
            throw BeanManagerLogger.LOG.injectionPointHasAmbiguousDependencies(Formats.formatAnnotations(ip.getQualifiers()),
                    Formats.formatInjectionPointType(ip.getType()), WeldCollections.toMultiRowString(allBeans()));
        }
    }

    private T getBeanInstance(Bean<?> bean) {
        final ThreadLocalStackReference<InjectionPoint> stack = currentInjectionPoint.pushConditionally(ip,
                isRegisterableInjectionPoint());
        try {
            return Reflections.<T> cast(getBeanManager().getReference(bean, getType(), getCreationalContext(), false));
        } finally {
            stack.pop();
        }
    }

    private boolean isRegisterableInjectionPoint() {
        return !getType().equals(InjectionPoint.class);
    }

    private Set<Bean<?>> allBeans() {
        return allBeans == null ? resolveBeans() : allBeans;
    }

    private Set<Bean<?>> resolveBeans() {
        // Perform typesafe resolution, and possibly attempt to resolve the ambiguity
        Resolvable resolvable = new ResolvableBuilder(getType(), getBeanManager()).addQualifiers(getQualifiers())
                .setDeclaringBean(getInjectionPoint().getBean()).create();
        TypeSafeBeanResolver beanResolver = getBeanManager().getBeanResolver();
        return beanResolver.resolve(beanResolver.resolve(resolvable, Reflections.isCacheable(getQualifiers())));
    }

    // Serialization
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
    }

    private static class SerializationProxy<T> extends AbstractFacadeSerializationProxy<T, Instance<T>> {

        private static final long serialVersionUID = 9181171328831559650L;

        public SerializationProxy(InstanceImpl<T> instance) {
            super(instance);
        }

        private Object readResolve() throws ObjectStreamException {
            return InstanceImpl.of(getInjectionPoint(), getCreationalContext(), getBeanManager());
        }

    }

    abstract class BeanIterator<TYPE> implements Iterator<TYPE> {

        protected final Iterator<Bean<?>> delegate;

        private BeanIterator(Set<Bean<?>> beans) {
            this.delegate = beans.iterator();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public void remove() {
            throw BeanLogger.LOG.instanceIteratorRemoveUnsupported();
        }

    }

    class InstanceImplIterator extends BeanIterator<T> {

        private InstanceImplIterator(Set<Bean<?>> beans) {
            super(beans);
        }

        @Override
        public T next() {
            return getBeanInstance(delegate.next());
        }
    }

    class HandleIterator extends BeanIterator<Handle<T>> {

        private HandleIterator(Set<Bean<?>> beans) {
            super(beans);
        }

        @Override
        public Handle<T> next() {
            Bean<?> bean = delegate.next();
            return new HandlerImpl<>(() -> getBeanInstance(bean), InstanceImpl.this, bean);
        }

    }

    private static class HandlerImpl<T> implements Handler<T> {

        private final LazyValueHolder<T> value;

        private final Bean<?> bean;

        private final WeakReference<WeldInstance<T>> instance;

        private final AtomicBoolean isDestroyed;

        HandlerImpl(Supplier<T> supplier, WeldInstance<T> instance, Bean<?> bean) {
            this.value = LazyValueHolder.forSupplier(supplier);
            this.bean = bean;
            this.instance = new WeakReference<>(instance);
            this.isDestroyed = new AtomicBoolean(false);
        }

        @Override
        public T get() {
            // attempting to resolve the contextual reference after it has been destroyed should throw an ISE
            if (isDestroyed.get()) {
                throw BeanLogger.LOG.tryingToResolveContextualReferenceAfterDestroyWasInvoked(this);
            }
            if (!value.isAvailable() && instance.get() == null) {
                // Contextual reference cannot be obtained if the producing Instance does not exist
                throw BeanLogger.LOG.cannotObtainHandlerContextualReference(this);
            }
            return value.get();
        }

        @Override
        public Bean<T> getBean() {
            // original API was returning Bean<?> whereas CDI API uses <Bean<T>>, we need to cast the result
            return (Bean<T>) bean;
        }

        @Override
        public void destroy() {
            WeldInstance<T> ref = instance.get();
            if (ref == null) {
                BeanLogger.LOG.cannotDestroyHandlerContextualReference(this);
            }
            if (value.isAvailable() && isDestroyed.compareAndSet(false, true)) {
                ref.destroy(value.get());
            }
        }

        @Override
        public void close() {
            destroy();
        }

        @Override
        public String toString() {
            return "HandlerImpl [bean=" + bean + "]";
        }

    }
}
