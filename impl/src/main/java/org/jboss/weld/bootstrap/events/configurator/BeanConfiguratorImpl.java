/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events.configurator;

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.bean.WeldBean;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentFinder;
import org.jboss.weld.bootstrap.event.WeldBeanConfigurator;
import org.jboss.weld.contexts.CreationalContextImpl;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.ForwardingWeldInstance;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class BeanConfiguratorImpl<T> implements WeldBeanConfigurator<T>, Configurator<Bean<T>> {

    private final BeanManagerImpl beanManager;

    private Class<?> beanClass;

    private final Set<InjectionPoint> injectionPoints;

    private final BeanAttributesConfiguratorImpl<T> attributes;

    private String id;

    private CreateCallback<T> createCallback;

    private DestroyCallback<T> destroyCallback;

    private Integer priority = null;

    /**
     *
     * @param defaultBeanClass
     * @param beanDeploymentFinder
     */
    public BeanConfiguratorImpl(Class<?> defaultBeanClass, Class<?> fallbackClass, BeanDeploymentFinder beanDeploymentFinder) {
        this.beanClass = defaultBeanClass;
        this.injectionPoints = new HashSet<>();
        BeanDeployment beanDeployment = beanDeploymentFinder.getBeanDeploymentIfExists(beanClass);
        if (beanDeployment == null && fallbackClass != null && BuildCompatibleExtension.class.isAssignableFrom(beanClass)) {
            // case of synth beans coming from BCE with no backing archive
            // use a fallback class, typically LiteExtensionTranslator.class
            beanDeployment = beanDeploymentFinder.getOrCreateBeanDeployment(fallbackClass);
        } else {
            beanDeployment = beanDeploymentFinder.getOrCreateBeanDeployment(beanClass);
        }
        this.beanManager = beanDeployment.getBeanManager();
        this.attributes = new BeanAttributesConfiguratorImpl<>(beanManager);
    }

    @Override
    public WeldBeanConfigurator<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> beanClass(Class<?> beanClass) {
        checkArgumentNotNull(beanClass);
        this.beanClass = beanClass;
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addInjectionPoint(InjectionPoint injectionPoint) {
        checkArgumentNotNull(injectionPoint);
        this.injectionPoints.add(injectionPoint);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addInjectionPoints(InjectionPoint... injectionPoints) {
        checkArgumentNotNull(injectionPoints);
        Collections.addAll(this.injectionPoints, injectionPoints);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addInjectionPoints(Set<InjectionPoint> injectionPoints) {
        checkArgumentNotNull(injectionPoints);
        this.injectionPoints.addAll(injectionPoints);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> injectionPoints(InjectionPoint... injectionPoints) {
        this.injectionPoints.clear();
        return addInjectionPoints(injectionPoints);
    }

    @Override
    public WeldBeanConfigurator<T> injectionPoints(Set<InjectionPoint> injectionPoints) {
        this.injectionPoints.clear();
        return addInjectionPoints(injectionPoints);
    }

    @Override
    public WeldBeanConfigurator<T> id(String id) {
        checkArgumentNotNull(id);
        this.id = id;
        return this;
    }

    @Override
    public <U extends T> WeldBeanConfigurator<U> createWith(Function<CreationalContext<U>, U> callback) {
        checkArgumentNotNull(callback);
        this.createCallback = cast(CreateCallback.fromCreateWith(callback));
        return cast(this);
    }

    @Override
    public <U extends T> WeldBeanConfigurator<U> produceWith(Function<Instance<Object>, U> callback) {
        checkArgumentNotNull(callback);
        this.createCallback = cast(CreateCallback.fromProduceWith(callback));
        return cast(this);
    }

    @Override
    public WeldBeanConfigurator<T> destroyWith(BiConsumer<T, CreationalContext<T>> callback) {
        checkArgumentNotNull(callback);
        this.destroyCallback = DestroyCallback.fromDestroy(callback);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> disposeWith(BiConsumer<T, Instance<Object>> callback) {
        checkArgumentNotNull(callback);
        this.destroyCallback = DestroyCallback.fromDispose(callback);
        return this;
    }

    @Override
    public <U extends T> WeldBeanConfigurator<U> read(AnnotatedType<U> type) {
        checkArgumentNotNull(type);
        final InjectionTarget<T> injectionTarget = cast(
                beanManager.getInjectionTargetFactory(type).createInjectionTarget(null));
        addInjectionPoints(injectionTarget.getInjectionPoints());
        createWith(c -> {
            T instance = injectionTarget.produce(c);
            injectionTarget.inject(instance, c);
            injectionTarget.postConstruct(instance);
            return instance;
        });
        destroyWith((i, c) -> {
            injectionTarget.preDestroy(i);
            c.release();
        });
        BeanAttributes<U> beanAttributes = beanManager.createBeanAttributes(type);
        read(beanAttributes);
        return cast(this);
    }

    @Override
    public WeldBeanConfigurator<T> read(BeanAttributes<?> beanAttributes) {
        checkArgumentNotNull(beanAttributes);
        this.attributes.read(beanAttributes);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addType(Type type) {
        checkArgumentNotNull(type);
        this.attributes.addType(type);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addType(TypeLiteral<?> typeLiteral) {
        checkArgumentNotNull(typeLiteral);
        this.attributes.addType(typeLiteral.getType());
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addTypes(Type... types) {
        checkArgumentNotNull(types);
        this.attributes.addTypes(types);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addTypes(Set<Type> types) {
        checkArgumentNotNull(types);
        this.attributes.addTypes(types);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addTransitiveTypeClosure(Type type) {
        checkArgumentNotNull(type);
        this.attributes.addTransitiveTypeClosure(type);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> types(Type... types) {
        checkArgumentNotNull(types);
        this.attributes.types(types);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> types(Set<Type> types) {
        checkArgumentNotNull(types);
        this.attributes.types(types);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> scope(Class<? extends Annotation> scope) {
        checkArgumentNotNull(scope);
        this.attributes.scope(scope);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addQualifier(Annotation qualifier) {
        checkArgumentNotNull(qualifier);
        this.attributes.addQualifier(qualifier);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addQualifiers(Annotation... qualifiers) {
        checkArgumentNotNull(qualifiers);
        this.attributes.addQualifiers(qualifiers);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addQualifiers(Set<Annotation> qualifiers) {
        checkArgumentNotNull(qualifiers);
        this.attributes.addQualifiers(qualifiers);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> qualifiers(Annotation... qualifiers) {
        checkArgumentNotNull(qualifiers);
        this.attributes.qualifiers(qualifiers);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> qualifiers(Set<Annotation> qualifiers) {
        checkArgumentNotNull(qualifiers);
        this.attributes.qualifiers(qualifiers);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addStereotype(Class<? extends Annotation> stereotype) {
        checkArgumentNotNull(stereotype);
        this.attributes.addStereotype(stereotype);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> addStereotypes(Set<Class<? extends Annotation>> stereotypes) {
        checkArgumentNotNull(stereotypes);
        this.attributes.addStereotypes(stereotypes);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> stereotypes(Set<Class<? extends Annotation>> stereotypes) {
        checkArgumentNotNull(stereotypes);
        this.attributes.stereotypes(stereotypes);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> name(String name) {
        this.attributes.name(name);
        return this;
    }

    @Override
    public WeldBeanConfigurator<T> alternative(boolean alternative) {
        this.attributes.alternative(alternative);
        return this;
    }

    @Override
    public BeanConfigurator<T> reserve(boolean value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Bean<T> complete() {
        if (createCallback == null) {
            // not callback specified, Weld does not know how to instantiate this new custom bean
            throw BeanLogger.LOG.noCallbackSpecifiedForCustomBean("bean [" + beanClass.toString()
                    + ", with types: " + Formats.formatTypes(attributes.types)
                    + ", and qualifiers: " + Formats.formatAnnotations(attributes.qualifiers) + "]");
        }
        return new ImmutableBean<>(this);
    }

    public BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    static final class CreateCallback<T> {

        private final Supplier<T> simple;

        private final Function<CreationalContext<T>, T> create;

        private final Function<Instance<Object>, T> instance;

        private CreationalContext<T> creationalContext;

        static <T> CreateCallback<T> fromProduceWith(Function<Instance<Object>, T> callback) {
            return new CreateCallback<T>(null, null, callback);
        }

        static <T> CreateCallback<T> fromProduceWith(Supplier<T> callback) {
            return new CreateCallback<T>(callback, null, null);
        }

        static <T> CreateCallback<T> fromCreateWith(Function<CreationalContext<T>, T> callback) {
            return new CreateCallback<T>(null, callback, null);
        }

        CreateCallback(Supplier<T> simple, Function<CreationalContext<T>, T> create, Function<Instance<Object>, T> instance) {
            this.simple = simple;
            this.create = create;
            this.instance = instance;
        }

        private T create(Bean<?> bean, CreationalContext<T> ctx, BeanManagerImpl beanManager) {
            this.creationalContext = ctx;
            if (simple != null) {
                return simple.get();
            } else if (instance != null) {
                return instance.apply(createInstance(bean, ctx, beanManager));
            } else {
                return create.apply(ctx);
            }
        }

        private Instance<Object> createInstance(Bean<?> bean, CreationalContext<T> ctx, BeanManagerImpl beanManager) {
            WeldInstance<Object> instance = beanManager.getInstance(ctx);
            if (Dependent.class.equals(bean.getScope())) {
                return instance;
            }
            return new GuardedInstance<>(bean, instance);
        }

        CreationalContext<T> getCreationalContext() {
            return creationalContext;
        }

    }

    static class GuardedInstance<T> extends ForwardingWeldInstance<T> {

        private final Bean<?> bean;

        private final WeldInstance<T> delegate;

        public GuardedInstance(Bean<?> bean, WeldInstance<T> delegate) {
            this.bean = bean;
            this.delegate = delegate;
        }

        @Override
        public WeldInstance<T> delegate() {
            return delegate;
        }

        @Override
        public <U extends T> WeldInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
            return wrap(subtype, delegate.select(subtype, qualifiers));
        }

        @Override
        public <U extends T> WeldInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            return wrap(subtype.getType(), delegate.select(subtype, qualifiers));
        }

        @Override
        public WeldInstance<T> select(Annotation... qualifiers) {
            return wrap(null, delegate.select(qualifiers));
        }

        @Override
        public <X> WeldInstance<X> select(Type subtype, Annotation... qualifiers) {
            return wrap(subtype, delegate.select(subtype, qualifiers));
        }

        private <TYPE> WeldInstance<TYPE> wrap(Type subtype, WeldInstance<TYPE> delegate) {
            if (subtype != null && InjectionPoint.class.equals(subtype)) {
                throw BeanLogger.LOG.cannotInjectInjectionPointMetadataIntoNonDependent(bean);
            }
            return new GuardedInstance<>(bean, delegate);
        }

    }

    static final class DestroyCallback<T> {

        private final BiConsumer<T, CreationalContext<T>> destroy;

        private final BiConsumer<T, Instance<Object>> dispose;

        static <T> DestroyCallback<T> fromDispose(BiConsumer<T, Instance<Object>> callback) {
            return new DestroyCallback<>(callback, null);
        }

        static <T> DestroyCallback<T> fromDestroy(BiConsumer<T, CreationalContext<T>> callback) {
            return new DestroyCallback<>(null, callback);
        }

        public DestroyCallback(BiConsumer<T, Instance<Object>> dispose, BiConsumer<T, CreationalContext<T>> destroy) {
            this.destroy = destroy;
            this.dispose = dispose;
        }

        void destroy(T instance, CreationalContext<T> ctx, BeanManagerImpl beanManager) {
            if (dispose != null) {
                dispose.accept(instance, beanManager.getInstance(ctx));
            } else {
                destroy.accept(instance, ctx);
            }
        }

    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <T> the class of the bean instance
     */
    static class ImmutableBean<T> extends ForwardingBeanAttributes<T> implements WeldBean<T>, PassivationCapable {

        private final String id;

        private final Integer priority;

        private final BeanManagerImpl beanManager;

        private final Class<?> beanClass;

        private final BeanAttributes<T> attributes;

        private final Set<InjectionPoint> injectionPoints;

        private final CreateCallback<T> createCallback;

        private final DestroyCallback<T> destroyCallback;

        /**
         *
         * @param configurator
         */
        ImmutableBean(BeanConfiguratorImpl<T> configurator) {
            this.beanManager = configurator.getBeanManager();
            this.beanClass = configurator.beanClass;
            this.attributes = configurator.attributes.complete();
            this.injectionPoints = ImmutableSet.copyOf(configurator.injectionPoints);
            this.createCallback = configurator.createCallback;
            this.destroyCallback = configurator.destroyCallback;
            this.priority = configurator.priority;
            if (configurator.id != null) {
                this.id = configurator.id;
            } else {
                this.id = BeanIdentifiers.forBuilderBean(attributes, beanClass);
            }
        }

        @Override
        public T create(CreationalContext<T> creationalContext) {
            return createCallback.create(this, creationalContext, beanManager);
        }

        @Override
        public void destroy(T instance, CreationalContext<T> creationalContext) {
            if (destroyCallback != null) {
                destroyCallback.destroy(instance, creationalContext, beanManager);
            }
            // release dependent beans from create/destroy callbacks
            if (creationalContext instanceof CreationalContextImpl) {
                // release dependent instances linked with this bean but avoid double invocation
                ((CreationalContextImpl<T>) creationalContext).release(this, instance);
                // in some cases, the CC for create and destroy callbacks might differ; hence this special handling
                CreationalContext<T> createCallbackCreationalContext = createCallback.getCreationalContext();
                if (createCallbackCreationalContext != null && createCallbackCreationalContext != creationalContext) {
                    createCallbackCreationalContext.release();
                }
            } else {
                creationalContext.release();
            }
        }

        @Override
        public Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public BeanIdentifier getIdentifier() {
            // this will likely never get called, so it's ok to create new object here
            return new StringBeanIdentifier(id);
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return injectionPoints;
        }

        @Override
        protected BeanAttributes<T> attributes() {
            return attributes;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Integer getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "Configurator Bean [" + getBeanClass().toString() + ", types: " + Formats.formatTypes(getTypes())
                    + ", qualifiers: "
                    + Formats.formatAnnotations(getQualifiers()) + "]";
        }

    }

}
