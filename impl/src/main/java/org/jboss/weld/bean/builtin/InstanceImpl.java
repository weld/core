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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.proxy.EnterpriseBeanInstance;
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
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeBeanResolver;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Helper implementation for Instance for getting instances
 *
 * @param <T>
 * @author Gavin King
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "SE_NO_SUITABLE_CONSTRUCTOR", "SE_BAD_FIELD" }, justification = "Uses SerializationProxy")
public class InstanceImpl<T> extends AbstractFacade<T, Instance<T>> implements WeldInstance<T>, Serializable {

    private static final long serialVersionUID = -376721889693284887L;

    private final transient Set<Bean<?>> allBeans;
    private final transient Bean<?> bean;

    private final transient CurrentInjectionPoint currentInjectionPoint;
    private final transient InjectionPoint ip;

    public static <I> Instance<I> of(InjectionPoint injectionPoint, CreationalContext<I> creationalContext,
            BeanManagerImpl beanManager) {
        return new InstanceImpl<I>(injectionPoint, creationalContext, beanManager);
    }

    private InstanceImpl(InjectionPoint injectionPoint, CreationalContext<? super T> creationalContext, BeanManagerImpl beanManager) {
        super(injectionPoint, creationalContext, beanManager);

        // Perform typesafe resolution, and possibly attempt to resolve the ambiguity
        Resolvable resolvable = new ResolvableBuilder(getType(), getBeanManager()).addQualifiers(getQualifiers())
                .setDeclaringBean(getInjectionPoint().getBean()).create();
        TypeSafeBeanResolver beanResolver = getBeanManager().getBeanResolver();
        this.allBeans = beanResolver.resolve(beanResolver.resolve(resolvable, Reflections.isCacheable(getQualifiers())));
        // optimization for the most common path - non-null bean means we are not unsatisfied not ambiguous
        if (allBeans.size() == 1) {
            this.bean = allBeans.iterator().next();
        } else {
            this.bean = null;
        }
        this.currentInjectionPoint = beanManager.getServices().get(CurrentInjectionPoint.class);
        // Generate a correct injection point for the bean, we do this by taking the original injection point and adjusting the
        // qualifiers and type
        this.ip = new DynamicLookupInjectionPoint(getInjectionPoint(), getType(), getQualifiers());
    }

    public T get() {
        if (bean != null) {
            return getBeanInstance(bean);
        } else if (isUnsatisfied()) {
            throw BeanManagerLogger.LOG.injectionPointHasUnsatisfiedDependencies(
                    Formats.formatAnnotations(ip.getQualifiers()),
                    Formats.formatInjectionPointType(ip.getType()),
                    InjectionPoints.getUnsatisfiedDependenciesAdditionalInfo(ip, getBeanManager()));
        } else {
            throw BeanManagerLogger.LOG.injectionPointHasAmbiguousDependencies(
                    Formats.formatAnnotations(ip.getQualifiers()),
                    Formats.formatInjectionPointType(ip.getType()),
                    WeldCollections.toMultiRowString(allBeans));
        }
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
        return new InstanceImplIterator(allBeans);
    }

    public boolean isAmbiguous() {
        return allBeans.size() > 1;
    }

    public boolean isUnsatisfied() {
        return allBeans.isEmpty();
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

    private <U extends T> WeldInstance<U> selectInstance(Type subtype, Annotation[] newQualifiers) {
        InjectionPoint modifiedInjectionPoint = new FacadeInjectionPoint(getBeanManager(), getInjectionPoint(), Instance.class, subtype, getQualifiers(),
                newQualifiers);
        return new InstanceImpl<U>(modifiedInjectionPoint, getCreationalContext(), getBeanManager());
    }

    @Override
    public void destroy(T instance) {
        Preconditions.checkNotNull(instance);
        // Attempt to destroy instance which is either a client proxy or a dependent session bean proxy
        if (instance instanceof ProxyObject) {
            ProxyObject proxy = (ProxyObject) instance;
            if (proxy.getHandler() instanceof ProxyMethodHandler) {
                ProxyMethodHandler handler = (ProxyMethodHandler) proxy.getHandler();
                Bean<?> bean = handler.getBean();
                if (instance instanceof EnterpriseBeanInstance && Dependent.class.equals(bean.getScope())) {
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
    public Handler<T> getHandler() {
        return new HandlerImpl<T>(get(), this, bean);
    }

    @Override
    public boolean isResolvable() {
        return !isUnsatisfied() && !isAmbiguous();
    }

    @Override
    public Iterable<Handler<T>> handlers() {
        return new Iterable<WeldInstance.Handler<T>>() {
            @Override
            public Iterator<org.jboss.weld.inject.WeldInstance.Handler<T>> iterator() {
                return new HandlerIterator(allBeans);
            }
        };
    }

    private void destroyDependentInstance(T instance) {
        CreationalContext<? super T> ctx = getCreationalContext();
        if (ctx instanceof WeldCreationalContext<?>) {
            WeldCreationalContext<? super T> weldCtx = cast(ctx);
            weldCtx.destroyDependentInstance(instance);
        }
    }

    private T getBeanInstance(Bean<?> bean) {
        final ThreadLocalStackReference<InjectionPoint> stack = currentInjectionPoint.push(ip);
        try {
            return Reflections.<T> cast(getBeanManager().getReference(bean, getType(), getCreationalContext(), false));
        } finally {
            stack.pop();
        }
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

    abstract class BeanIterator<I> implements Iterator<I> {

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

    class HandlerIterator extends BeanIterator<Handler<T>> {

        private HandlerIterator(Set<Bean<?>> beans) {
            super(beans);
        }

        @Override
        public Handler<T> next() {
            Bean<?> bean = delegate.next();
            return new HandlerImpl<>(getBeanInstance(bean), InstanceImpl.this, bean);
        }

    }

    private static class HandlerImpl<T> implements Handler<T> {

        private final T value;

        private final Bean<?> bean;

        private final WeakReference<WeldInstance<T>> weldInstance;

        private boolean destroyed;

        HandlerImpl(T value, WeldInstance<T> instance, Bean<?> bean) {
            this.value = value;
            this.bean = bean;
            this.weldInstance = new WeakReference<>(instance);
            this.destroyed = false;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Bean<?> getBean() {
            return bean;
        }

        @Override
        public void destroy() {
            WeldInstance<T> instance = weldInstance.get();
            if (instance == null || destroyed) {
                return;
            }
            instance.destroy(value);
            destroyed = true;
        }

        @Override
        public void close() {
            destroy();
        }

    }

}
