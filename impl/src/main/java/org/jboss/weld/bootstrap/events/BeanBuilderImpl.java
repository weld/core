/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.experimental.BeanBuilder;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.DeploymentStructures;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

/**
 *
 *
 * @author Martin Kouba
 * @param <T> the class of the bean instance
 */
public final class BeanBuilderImpl<T> extends BeanAttributesBuilder<T, BeanBuilder<T>> implements BeanBuilder<T> {

    private static final String CALLBACK_PARAM = "callback";

    private final DeploymentFinder deploymentFinder;

    private BeanManagerImpl beanManager;

    private String id;

    private Class<?> beanClass;

    private Set<InjectionPoint> injectionPoints;

    private CreateCallback<T> createCallback;

    private DestroyCallback<T> destroyCallback;

    /**
     *
     * @param extensionClass
     * @param bdaMapping
     * @param deployment
     * @param contexts
     * @param deploymentManager
     */
    public BeanBuilderImpl(Class<? extends Extension> extensionClass, BeanDeploymentArchiveMapping bdaMapping, Deployment deployment,
            Collection<ContextHolder<? extends Context>> contexts, BeanManagerImpl deploymentManager) {
        super();
        Preconditions.checkArgumentNotNull(extensionClass, "extensionClass");
        Preconditions.checkArgumentNotNull(bdaMapping, "bdaMapping");
        Preconditions.checkArgumentNotNull(deployment, "deployment");
        Preconditions.checkArgumentNotNull(contexts, "contexts");
        Preconditions.checkArgumentNotNull(deploymentManager, "deploymentManager");
        this.deploymentFinder = new DeploymentFinder(bdaMapping, deployment, contexts, deploymentManager);
        beanClass(extensionClass);
        this.injectionPoints = new HashSet<InjectionPoint>();
    }

    /**
     *
     * @return
     */
    public Bean<T> build() {
        validate();
        return new ImmutableBean<T>(beanManager, id, beanClass, super.build(), injectionPoints, createCallback, destroyCallback);
    }

    @Override
    public <U extends T> BeanBuilder<U> read(AnnotatedType<U> type) {
        Preconditions.checkArgumentNotNull(type, "type");

        beanClass(type.getJavaClass());

        final InjectionTarget<T> injectionTarget = cast(beanManager.getInjectionTargetFactory(type).createInjectionTarget(null));
        injectionPoints(injectionTarget.getInjectionPoints());
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
        return cast(read(beanManager.createBeanAttributes(type)));
    }

    @Override
    public BeanBuilder<T> read(BeanAttributes<?> beanAttributes) {
        Preconditions.checkArgumentNotNull(beanAttributes, "beanAttributes");

        scope(beanAttributes.getScope());
        name(beanAttributes.getName());
        alternative(beanAttributes.isAlternative());
        qualifiers(beanAttributes.getQualifiers());
        stereotypes(beanAttributes.getStereotypes());
        types(beanAttributes.getTypes());

        return this;
    }

    public BeanBuilder<T> beanClass(Class<?> beanClass) {
        Preconditions.checkArgumentNotNull(beanClass, "beanClass");
        this.beanClass = beanClass;
        this.beanManager = deploymentFinder.getOrCreateBeanDeployment(beanClass).getBeanManager();
        return this;
    }

    public BeanBuilder<T> addInjectionPoint(InjectionPoint injectionPoint) {
        injectionPoints.add(injectionPoint);
        return this;
    }

    public BeanBuilder<T> injectionPoints(InjectionPoint... injectionPoints) {
        this.injectionPoints = new HashSet<InjectionPoint>();
        Collections.addAll(this.injectionPoints, injectionPoints);
        return this;
    }

    @Override
    public BeanBuilder<T> injectionPoints(Set<InjectionPoint> injectionPoints) {
        Preconditions.checkArgumentNotNull(injectionPoints, "injectionPoints");
        this.injectionPoints = new HashSet<InjectionPoint>(injectionPoints);
        return this;
    }

    public BeanBuilder<T> id(String id) {
        Preconditions.checkArgumentNotNull(id, "id");
        this.id = id;
        return this;
    }

    public <U extends T> BeanBuilder<U> createWith(Function<CreationalContext<U>, U> callback) {
        Preconditions.checkArgumentNotNull(callback, CALLBACK_PARAM);
        this.createCallback = cast(CreateCallback.fromCreateWith(callback));
        return cast(this);
    }

    @Override
    public <U extends T> BeanBuilder<U> produceWith(Function<Instance<Object>, U> callback) {
        Preconditions.checkArgumentNotNull(callback, CALLBACK_PARAM);
        this.createCallback = cast(CreateCallback.fromProduceWith(callback));
        if (this.destroyCallback == null) {
            this.destroyCallback = new DestroyCallback<T>((i) -> {
            });
        }
        return cast(this);
    }

    @Override
    public <U extends T> BeanBuilder<U> produceWith(Supplier<U> callback) {
        Preconditions.checkArgumentNotNull(callback, CALLBACK_PARAM);
        this.createCallback = cast(CreateCallback.fromProduceWith(callback));
        if (this.destroyCallback == null) {
            this.destroyCallback = new DestroyCallback<T>((i) -> {
            });
        }
        return cast(this);
    }

    public BeanBuilder<T> destroyWith(BiConsumer<T, CreationalContext<T>> callback) {
        Preconditions.checkArgumentNotNull(callback, CALLBACK_PARAM);
        this.destroyCallback = new DestroyCallback<>(callback);
        return this;
    }

    @Override
    public BeanBuilder<T> disposeWith(Consumer<T> callback) {
        Preconditions.checkArgumentNotNull(callback, CALLBACK_PARAM);
        this.destroyCallback = new DestroyCallback<>(callback);
        return this;
    }

    @Override
    protected BeanBuilder<T> self() {
        return this;
    }

    BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    void validate() {
        if (createCallback == null) {
            throw BeanLogger.LOG.beanBuilderInvalidCreateCallback(this);
        }
        if (destroyCallback == null) {
            throw BeanLogger.LOG.beanBuilderInvalidDestroyCallback(this);
        }
    }

    @Override
    public String toString() {
        return String.format("BeanBuilderImpl [id=%s, beanClass=%s, qualifiers=%s, types=%s]", id, beanClass, qualifiers, types);
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <T> the class of the bean instance
     */
    static class ImmutableBean<T> extends ForwardingBeanAttributes<T> implements Bean<T>, PassivationCapable {

        private final String id;

        private final BeanManagerImpl beanManager;

        private final Class<?> beanClass;

        private final BeanAttributes<T> attributes;

        private final Set<InjectionPoint> injectionPoints;

        private final CreateCallback<T> createCallback;

        private final DestroyCallback<T> destroyCallback;

        /**
         *
         * @param beanManager
         * @param beanClass
         * @param attributes
         * @param injectionPoints
         * @param createCallback
         * @param destroyCallback
         */
        ImmutableBean(BeanManagerImpl beanManager, String id, Class<?> beanClass, BeanAttributes<T> attributes, Set<InjectionPoint> injectionPoints,
                CreateCallback<T> createCallback, DestroyCallback<T> destroyCallback) {
            this.beanManager = beanManager;
            this.beanClass = beanClass;
            this.attributes = attributes;
            this.injectionPoints = ImmutableSet.copyOf(injectionPoints);
            this.createCallback = createCallback;
            this.destroyCallback = destroyCallback;
            if (id != null) {
                this.id = id;
            } else {
                this.id = BeanIdentifiers.forBuilderBean(attributes, beanClass);
            }
        }

        @Override
        public T create(CreationalContext<T> creationalContext) {
            return createCallback.create(creationalContext, beanManager);
        }

        @Override
        public void destroy(T instance, CreationalContext<T> creationalContext) {
            destroyCallback.destroy(instance, creationalContext);
        }

        @Override
        public Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return injectionPoints;
        }

        @Override
        public boolean isNullable() {
            return false;
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
        public String toString() {
            return "Immutable Builder Bean [" + getBeanClass().toString() + "] with types [" + Formats.formatTypes(getTypes()) + "] with qualifiers ["
                    + Formats.formatAnnotations(getQualifiers()) + "]";
        }

    }

    static class CreateCallback<T> {

        private final Supplier<T> simple;

        private final Function<CreationalContext<T>, T> create;

        private final Function<Instance<Object>, T> instance;

        static <T> CreateCallback<T> fromProduceWith(Function<Instance<Object>, T> callback) {
            return new CreateCallback<T>(null, null, callback);
        }

        static <T> CreateCallback<T> fromProduceWith(Supplier<T> callback) {
            return new CreateCallback<T>(callback, null, null);
        }

        static <T> CreateCallback<T> fromCreateWith(Function<CreationalContext<T>, T> callback) {
            return new CreateCallback<T>(null, callback, null);
        }

        public CreateCallback(Supplier<T> simple, Function<CreationalContext<T>, T> create, Function<Instance<Object>, T> instance) {
            this.simple = simple;
            this.create = create;
            this.instance = instance;
        }

        T create(CreationalContext<T> ctx, BeanManagerImpl beanManager) {
            if (simple != null) {
                return simple.get();
            } else if (instance != null) {
                return instance.apply(beanManager.getInstance(ctx));
            } else {
                return create.apply(ctx);
            }
        }

    }

    static class DestroyCallback<T> {

        private final BiConsumer<T, CreationalContext<T>> destroy;

        private final Consumer<T> simple;

        public DestroyCallback(Consumer<T> callback) {
            this.destroy = null;
            this.simple = callback;
        }

        public DestroyCallback(BiConsumer<T, CreationalContext<T>> callback) {
            this.destroy = callback;
            this.simple = null;
        }

        void destroy(T instance, CreationalContext<T> ctx) {
            if (simple != null) {
                simple.accept(instance);
            } else {
                destroy.accept(instance, ctx);
            }
        }

    }

    static class DeploymentFinder {

        private final BeanDeploymentArchiveMapping bdaMapping;

        private final Deployment deployment;

        private final Collection<ContextHolder<? extends Context>> contexts;

        private final BeanManagerImpl deploymentManager;

        DeploymentFinder(BeanDeploymentArchiveMapping bdaMapping, Deployment deployment, Collection<ContextHolder<? extends Context>> contexts,
                BeanManagerImpl deploymentManager) {
            this.bdaMapping = bdaMapping;
            this.deployment = deployment;
            this.contexts = contexts;
            this.deploymentManager = deploymentManager;
        }

        BeanDeployment getOrCreateBeanDeployment(Class<?> clazz) {
            return DeploymentStructures.getOrCreateBeanDeployment(deployment, deploymentManager, bdaMapping, contexts, clazz);
        }

    }

}
