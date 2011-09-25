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
package org.jboss.weld.manager;

import static org.jboss.weld.logging.messages.BeanManagerMessage.AMBIGUOUS_BEANS_FOR_DEPENDENCY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.CONTEXT_NOT_ACTIVE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_ACTIVE_CONTEXTS;
import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_INTERCEPTOR_BINDING;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_BINDINGS_EMPTY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NON_NORMAL_SCOPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NOT_INTERCEPTOR_BINDING_TYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NOT_STEREOTYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NO_DECORATOR_TYPES;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_BEAN_ARGUMENT;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_BEAN_TYPE_ARGUMENT;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_CREATIONAL_CONTEXT_ARGUMENT;
import static org.jboss.weld.logging.messages.BeanManagerMessage.SPECIFIED_TYPE_NOT_BEAN_TYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.TOO_MANY_ACTIVITIES;
import static org.jboss.weld.logging.messages.BeanManagerMessage.UNPROXYABLE_RESOLUTION;
import static org.jboss.weld.logging.messages.BeanManagerMessage.UNRESOLVABLE_ELEMENT;
import static org.jboss.weld.manager.BeanManagers.buildAccessibleClosure;
import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.jboss.weld.util.reflection.Reflections.isCacheable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.Container;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bean.builtin.InstanceImpl;
import org.jboss.weld.bean.proxy.ClientProxyProvider;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.AbstractProcessInjectionTarget;
import org.jboss.weld.context.ContextNotActiveException;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.el.Namespace;
import org.jboss.weld.el.WeldELResolver;
import org.jboss.weld.el.WeldExpressionFactory;
import org.jboss.weld.event.ObserverNotifier;
import org.jboss.weld.exceptions.AmbiguousResolutionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.InjectionException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.interceptor.InterceptorBindingType;
import org.jboss.weld.interceptor.reader.cache.DefaultMetadataCachingReader;
import org.jboss.weld.interceptor.reader.cache.MetadataCachingReader;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.ScopeModel;
import org.jboss.weld.metadata.cache.StereotypeModel;
import org.jboss.weld.resolution.InterceptorResolvable;
import org.jboss.weld.resolution.InterceptorResolvableBuilder;
import org.jboss.weld.resolution.NameBasedResolver;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeBeanResolver;
import org.jboss.weld.resolution.TypeSafeDecoratorResolver;
import org.jboss.weld.resolution.TypeSafeInterceptorResolver;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resolution.TypeSafeResolver;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.collections.IterableToIteratorFunction;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.Iterators;

/**
 * Implementation of the Bean Manager.
 * <p/>
 * Essentially a singleton for registering Beans, Contexts, Observers,
 * Interceptors etc. as well as providing resolution
 *
 * @author Pete Muir
 * @author Marius Bogoevici
 * @author Ales Justin
 * @author Marko Luksa
 */
public class BeanManagerImpl implements WeldManager, Serializable {

    private static final long serialVersionUID = 3021562879133838561L;

    /*
    * Application scoped services
    * ***************************
    */
    private final transient ServiceRegistry services;

    /*
    * Application scoped data structures
    * ***********************************
    */

    // Contexts are shared across the application
    private final transient Map<Class<? extends Annotation>, List<Context>> contexts;

    // Client proxies can be used application wide
    private final transient ClientProxyProvider clientProxyProvider;

    // TODO review this structure
    private final transient Map<EjbDescriptor<?>, SessionBean<?>> enterpriseBeans;

    /*
    * Archive scoped data structures
    * ******************************
    */

    /* These data structures are all non-transitive in terms of bean deployment
    * archive accessibility, and the configuration for this bean deployment
    * archive
    */
    private final transient Enabled enabled;
    private final transient Set<CurrentActivity> currentActivities;


    /*
    * Activity scoped services
    * *************************
    */

    /* These services are scoped to this activity only, but use data
    * structures that are transitive accessible from other bean deployment
    * archives
    */
    private final transient TypeSafeBeanResolver<Bean<?>> beanResolver;
    private final transient TypeSafeResolver<Resolvable, Decorator<?>> decoratorResolver;
    private final transient TypeSafeResolver<InterceptorResolvable, Interceptor<?>> interceptorResolver;
    private final transient NameBasedResolver nameBasedResolver;
    private final transient ELResolver weldELResolver;
    private transient Namespace rootNamespace;

    private final transient ObserverNotifier accessibleObserverNotifier;

    /*
    * Activity scoped data structures
    * ********************************
    */

    /* These data structures are scoped to this bean deployment archive activity
    * only and represent the beans, decorators, interceptors, namespaces and
    * observers deployed in this bean deployment archive activity
    */
    private final transient List<Bean<?>> beans;
    private final transient List<Bean<?>> transitiveBeans;
    private final transient List<Decorator<?>> decorators;
    private final transient List<Interceptor<?>> interceptors;
    private final transient List<String> namespaces;
    private final transient List<ObserverMethod<?>> observers;

    /*
    * set that is only used to make sure that no duplicate beans are added
    */
    private final transient Set<Bean<?>> beanSet = Collections.synchronizedSet(new HashSet<Bean<?>>());

    /*
    * These data structures represent the managers *accessible* from this bean
    * deployment archive activity
    */
    private final transient HashSet<BeanManagerImpl> accessibleManagers;

    /*
    * This data structures represents child activities for this activity, it is
    * not transitively accessible
    */
    private final transient Set<BeanManagerImpl> childActivities;

    private final AtomicInteger childIds;
    private final String id;
    private final String contextId;

    /**
     * Interception model
     */
    private final transient Map<Class<?>, InterceptionModel<ClassMetadata<?>, ?>> interceptorModelRegistry = new ConcurrentHashMap<Class<?>, InterceptionModel<ClassMetadata<?>, ?>>();
    private final transient MetadataCachingReader interceptorMetadataReader = new DefaultMetadataCachingReader();

    /**
     * Beans closure
     */
    private final transient BeansClosure closure;

    /**
     * Create a new, root, manager
     *
     * @param serviceRegistry
     * @return
     */
    public static BeanManagerImpl newRootManager(String contextId, String id, ServiceRegistry serviceRegistry, Enabled enabled) {
        Map<Class<? extends Annotation>, List<Context>> contexts = new ConcurrentHashMap<Class<? extends Annotation>, List<Context>>();

        return new BeanManagerImpl(
                serviceRegistry,
                new CopyOnWriteArrayList<Bean<?>>(),
                new CopyOnWriteArrayList<Bean<?>>(),
                new CopyOnWriteArrayList<Decorator<?>>(),
                new CopyOnWriteArrayList<Interceptor<?>>(),
                new CopyOnWriteArrayList<ObserverMethod<?>>(),
                new CopyOnWriteArrayList<String>(),
                new ConcurrentHashMap<EjbDescriptor<?>, SessionBean<?>>(),
                new ClientProxyProvider(contextId),
                contexts,
                new CopyOnWriteArraySet<CurrentActivity>(),
                enabled,
                id, contextId,
                new AtomicInteger());
    }

    public static BeanManagerImpl newManager(BeanManagerImpl rootManager, String id, ServiceRegistry services, Enabled enabled) {
        return new BeanManagerImpl(
                services,
                new CopyOnWriteArrayList<Bean<?>>(),
                new CopyOnWriteArrayList<Bean<?>>(),
                new CopyOnWriteArrayList<Decorator<?>>(),
                new CopyOnWriteArrayList<Interceptor<?>>(),
                new CopyOnWriteArrayList<ObserverMethod<?>>(),
                new CopyOnWriteArrayList<String>(),
                rootManager.getEnterpriseBeans(),
                rootManager.getClientProxyProvider(),
                rootManager.getContexts(),
                new CopyOnWriteArraySet<CurrentActivity>(),
                enabled,
                id, rootManager.contextId,
                new AtomicInteger());
    }

    /**
     * Create a new child manager
     *
     * @param parentManager the parent manager
     * @return new child manager
     */
    public static BeanManagerImpl newChildActivityManager(BeanManagerImpl parentManager) {
        List<Bean<?>> beans = new CopyOnWriteArrayList<Bean<?>>();
        beans.addAll(parentManager.getBeans());
        List<Bean<?>> transitiveBeans = new CopyOnWriteArrayList<Bean<?>>();
        beans.addAll(parentManager.getTransitiveBeans());

        List<ObserverMethod<?>> registeredObservers = new CopyOnWriteArrayList<ObserverMethod<?>>();
        registeredObservers.addAll(parentManager.getObservers());
        List<String> namespaces = new CopyOnWriteArrayList<String>();
        namespaces.addAll(parentManager.getNamespaces());

        return new BeanManagerImpl(
                parentManager.getServices(),
                beans,
                transitiveBeans,
                parentManager.getDecorators(),
                parentManager.getInterceptors(),
                registeredObservers,
                namespaces,
                parentManager.getEnterpriseBeans(),
                parentManager.getClientProxyProvider(),
                parentManager.getContexts(),
                parentManager.getCurrentActivities(),
                parentManager.getEnabled(),
                new StringBuilder().append(parentManager.getChildIds().incrementAndGet()).toString(),
                parentManager.contextId,
                parentManager.getChildIds());
    }

    private BeanManagerImpl(
            ServiceRegistry serviceRegistry,
            List<Bean<?>> beans,
            List<Bean<?>> transitiveBeans,
            List<Decorator<?>> decorators,
            List<Interceptor<?>> interceptors,
            List<ObserverMethod<?>> observers,
            List<String> namespaces,
            Map<EjbDescriptor<?>, SessionBean<?>> enterpriseBeans,
            ClientProxyProvider clientProxyProvider,
            Map<Class<? extends Annotation>, List<Context>> contexts,
            Set<CurrentActivity> currentActivities,
            Enabled enabled,
            String id, String contextId,
            AtomicInteger childIds) {
        this.services = serviceRegistry;
        this.beans = beans;
        this.transitiveBeans = transitiveBeans;
        this.decorators = decorators;
        this.interceptors = interceptors;
        this.enterpriseBeans = enterpriseBeans;
        this.clientProxyProvider = clientProxyProvider;
        this.contexts = contexts;
        this.currentActivities = currentActivities;
        this.observers = observers;
        this.enabled = enabled;
        this.namespaces = namespaces;
        this.id = id;
        this.contextId = contextId;
        this.childIds = new AtomicInteger();

        // Set up the structure to store accessible managers in
        this.accessibleManagers = new HashSet<BeanManagerImpl>();

        // TODO Currently we build the accessible bean list on the fly, we need to set it in stone once bootstrap is finished...
        Transform<Bean<?>> beanTransform = new BeanTransform(this);
        this.beanResolver = new TypeSafeBeanResolver<Bean<?>>(this, createDynamicAccessibleIterable(beanTransform));
        this.decoratorResolver = new TypeSafeDecoratorResolver(this, createDynamicAccessibleIterable(DecoratorTransform.INSTANCE));
        this.interceptorResolver = new TypeSafeInterceptorResolver(this, createDynamicAccessibleIterable(InterceptorTransform.INSTANCE));
        this.nameBasedResolver = new NameBasedResolver(this, createDynamicAccessibleIterable(beanTransform));
        this.weldELResolver = new WeldELResolver(this);
        this.childActivities = new CopyOnWriteArraySet<BeanManagerImpl>();
        TypeSafeObserverResolver observerResolver = new TypeSafeObserverResolver(this, createDynamicAccessibleIterable(ObserverMethodTransform.INSTANCE));
        this.accessibleObserverNotifier = ObserverNotifier.of(observerResolver, getServices());
        this.closure = new BeansClosure(this);
    }

    public BeansClosure getClosure() {
        return closure;
    }

    public String getContextId() {
        return contextId;
    }

    private <T> Iterable<T> createDynamicAccessibleIterable(final Transform<T> transform) {
        return new Iterable<T>() {

            public Iterator<T> iterator() {
                Set<Iterable<T>> iterable = buildAccessibleClosure(BeanManagerImpl.this, transform);
                return Iterators.concat(Iterators.transform(iterable.iterator(), IterableToIteratorFunction.<T>instance()));
            }

        };
    }

    public void addAccessibleBeanManager(BeanManagerImpl accessibleBeanManager) {
        accessibleManagers.add(accessibleBeanManager);
        beanResolver.clear();
        interceptorResolver.clear();
        decoratorResolver.clear();
        accessibleObserverNotifier.clear();
    }

    public HashSet<BeanManagerImpl> getAccessibleManagers() {
        return accessibleManagers;
    }

    public void addBean(Bean<?> bean) {
        if (beanSet.contains(bean)) {
            return;
        }
        if (bean.getClass().equals(SessionBean.class)) {
            SessionBean<?> enterpriseBean = (SessionBean<?>) bean;
            enterpriseBeans.put(enterpriseBean.getEjbDescriptor(), enterpriseBean);
        }
        if (bean instanceof PassivationCapable) {
            Container.instance(contextId).services().get(ContextualStore.class).putIfAbsent(bean);
        }
        registerBeanNamespace(bean);
        for (BeanManagerImpl childActivity : childActivities) {
            childActivity.addBean(bean);
        }
        // New beans (except for SessionBeans) and most built in beans aren't resolvable transtively
        if (bean instanceof ExtensionBean || bean instanceof SessionBean || (!(bean instanceof NewBean) && !(bean instanceof AbstractBuiltInBean<?>))) {
            this.transitiveBeans.add(bean);
        }
        this.beans.add(bean);
        this.beanSet.add(bean);
    }

    public void addDecorator(Decorator<?> bean) {
        decorators.add(bean);
        getServices().get(ContextualStore.class).putIfAbsent(bean);
        decoratorResolver.clear();
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... bindings) {
        return accessibleObserverNotifier.resolveObserverMethods(event, bindings);
    }

    public void addInterceptor(Interceptor<?> bean) {
        interceptors.add(bean);
        getServices().get(ContextualStore.class).putIfAbsent(bean);
        interceptorResolver.clear();
    }

    /**
     * Enabled Alternatives, Interceptors and Decorators
     *
     * @return
     */
    public Enabled getEnabled() {
        return enabled;
    }

    public boolean isBeanEnabled(Bean<?> bean) {
        return Beans.isBeanEnabled(bean, getEnabled());
    }

    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        Resolvable resolvable = new ResolvableBuilder(beanType, this).addQualifiers(qualifiers).create();
        return beanResolver.resolve(resolvable, isCacheable(qualifiers));
    }

    public Set<Bean<?>> getBeans(Type beanType, Set<Annotation> qualifiers) {
        return beanResolver.resolve(new ResolvableBuilder(beanType, this).addQualifiers(qualifiers).create(), isCacheable(qualifiers));
    }

    public Set<Bean<?>> getBeans(InjectionPoint injectionPoint) {
        boolean registerInjectionPoint = !injectionPoint.getType().equals(InjectionPoint.class);
        CurrentInjectionPoint currentInjectionPoint = null;
        if (registerInjectionPoint) {
            currentInjectionPoint = Container.instance(contextId).services().get(CurrentInjectionPoint.class);
            currentInjectionPoint.push(injectionPoint);
        }
        try {
            // We always cache, we assume that people don't use inline annotation literal declarations, a little risky but FAQd
            return beanResolver.resolve(new ResolvableBuilder(injectionPoint, this).create(), true);
        } finally {
            if (registerInjectionPoint) {
                currentInjectionPoint.pop();
            }
        }
    }

    protected void registerBeanNamespace(Bean<?> bean) {
        if (bean.getName() != null && bean.getName().indexOf('.') > 0) {
            namespaces.add(bean.getName().substring(0, bean.getName().lastIndexOf('.')));
        }
    }

    /**
     * Gets the class-mapped beans. For internal use.
     *
     * @return The bean map
     */
    public Map<EjbDescriptor<?>, SessionBean<?>> getEnterpriseBeans() {
        return enterpriseBeans;
    }

    /**
     * The beans registered with the Web Bean manager which are resolvable. Does
     * not include interceptor and decorator beans
     *
     * @return The list of known beans
     */
    public List<Bean<?>> getBeans() {
        return Collections.unmodifiableList(beans);
    }

    List<Bean<?>> getTransitiveBeans() {
        return Collections.unmodifiableList(transitiveBeans);
    }

    public List<Decorator<?>> getDecorators() {
        return Collections.unmodifiableList(decorators);
    }

    public List<Interceptor<?>> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    public Iterable<Bean<?>> getAccessibleBeans() {
        return createDynamicAccessibleIterable(new BeanTransform(this));
    }

    public Iterable<Interceptor<?>> getAccessibleInterceptors() {
        return createDynamicAccessibleIterable(new InterceptorTransform());
    }

    public Iterable<Decorator<?>> getAccessibleDecorators() {
        return createDynamicAccessibleIterable(new DecoratorTransform());
    }

    public void addContext(Context context) {
        Class<? extends Annotation> scope = context.getScope();
        List<Context> contextList = contexts.get(scope);
        if (contextList == null) {
            contextList = new CopyOnWriteArrayList<Context>();
            contexts.put(scope, contextList);
        }
        contextList.add(context);
    }

    /**
     * Does the actual observer registration
     *
     * @param observer =
     */
    public void addObserver(ObserverMethod<?> observer) {
        //checkEventType(observer.getObservedType());
        observers.add(observer);
        for (BeanManagerImpl childActivity : childActivities) {
            childActivity.addObserver(observer);
        }
    }

    /**
     * Fires an event object with given event object for given bindings
     *
     * @param event      The event object to pass along
     * @param qualifiers The binding types to match
     * @see javax.enterprise.inject.spi.BeanManager#fireEvent(java.lang.Object,
     *      java.lang.annotation.Annotation[])
     */
    public void fireEvent(Object event, Annotation... qualifiers) {
        accessibleObserverNotifier.fireEvent(event, qualifiers);
    }

    /**
     * Gets an active context of the given scope. Throws an exception if there
     * are no active contexts found or if there are too many matches
     *
     * @param scopeType The scope to match
     * @return A single active context of the given scope
     * @see javax.enterprise.inject.spi.BeanManager#getContext(java.lang.Class)
     */
    public Context getContext(Class<? extends Annotation> scopeType) {
        Context activeContext = null;
        final List<Context> ctx = contexts.get(scopeType);
        if (ctx == null) {
            //this happens if no context is registered
            throw new ContextNotActiveException(CONTEXT_NOT_ACTIVE, scopeType.getName());
        }
        for (Context context : ctx) {
            if (context.isActive()) {
                if (activeContext == null) {
                    activeContext = context;
                } else {
                    throw new IllegalStateException(DUPLICATE_ACTIVE_CONTEXTS, scopeType.getName());
                }
            }
        }
        if (activeContext == null) {
            throw new ContextNotActiveException(CONTEXT_NOT_ACTIVE, scopeType.getName());
        }

        return activeContext;
    }

    public Object getReference(Bean<?> bean, CreationalContext<?> creationalContext, boolean noProxy) {
        bean = getMostSpecializedBean(bean);
        if (creationalContext instanceof WeldCreationalContext<?>) {
            creationalContext = ((WeldCreationalContext<?>) creationalContext).getCreationalContext(bean);
        }
        if (!noProxy && isProxyRequired(bean)) {
            if (creationalContext != null || getContext(bean.getScope()).get(bean) != null) {
                return clientProxyProvider.getClientProxy(bean);
            } else {
                return null;
            }
        } else {
            return getContext(bean.getScope()).get(Reflections.<Contextual>cast(bean), creationalContext);
        }
    }

    private boolean isProxyRequired(Bean<?> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isProxyRequired();
        } else if (isNormalScoped(bean)) {
            return true;
        } else {
            return false;
        }
    }

    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> creationalContext) {
        if (bean == null) {
            throw new IllegalArgumentException(NULL_BEAN_ARGUMENT);
        }
        if (beanType == null) {
            throw new IllegalArgumentException(NULL_BEAN_TYPE_ARGUMENT);
        }
        if (creationalContext == null) {
            throw new IllegalArgumentException(NULL_CREATIONAL_CONTEXT_ARGUMENT);
        }
        if (!Reflections.matches(beanType, bean.getTypes())) {
            throw new IllegalArgumentException(SPECIFIED_TYPE_NOT_BEAN_TYPE, beanType, bean);
        }
        return getReference(bean, creationalContext, false);
    }


    /**
     * Get a reference, registering the injection point used.
     *
     * @param injectionPoint    the injection point to register
     * @param resolvedBean      the bean to get a reference to
     * @param creationalContext the creationalContext
     * @return
     */
    public Object getReference(InjectionPoint injectionPoint, Bean<?> resolvedBean, CreationalContext<?> creationalContext) {
        if (resolvedBean == null) {
            throw new IllegalArgumentException(NULL_BEAN_ARGUMENT);
        }
        if (creationalContext == null) {
            throw new IllegalArgumentException(NULL_CREATIONAL_CONTEXT_ARGUMENT);
        }
        boolean registerInjectionPoint = injectionPoint != null && !injectionPoint.getType().equals(InjectionPoint.class);
        boolean delegateInjectionPoint = injectionPoint != null && injectionPoint.isDelegate();

        final ServiceRegistry services = Container.instance(contextId).services();
        CurrentInjectionPoint currentInjectionPoint = null;
        if (registerInjectionPoint) {
            currentInjectionPoint = services.get(CurrentInjectionPoint.class);
            currentInjectionPoint.push(injectionPoint);
        }
        try {
            if (injectionPoint != null && isNormalScoped(resolvedBean) && !Proxies.isTypeProxyable(injectionPoint.getType())) {
                throw new UnproxyableResolutionException(UNPROXYABLE_RESOLUTION, resolvedBean, injectionPoint);
            }
            // TODO Can we move this logic to getReference?
            if (creationalContext instanceof WeldCreationalContext<?>) {
                WeldCreationalContext<?> wbCreationalContext = (WeldCreationalContext<?>) creationalContext;
                final Object incompleteInstance = wbCreationalContext.getIncompleteInstance(resolvedBean);
                if (incompleteInstance != null) {
                    return incompleteInstance;
                } else {
                    return getReference(resolvedBean, wbCreationalContext, delegateInjectionPoint);
                }
            } else {
                return getReference(resolvedBean, creationalContext, delegateInjectionPoint);
            }
        } finally {
            if (registerInjectionPoint) {
                currentInjectionPoint.pop();
            }
        }
    }

    public Object getInjectableReference(InjectionPoint injectionPoint, CreationalContext<?> creationalContext) {
        if (injectionPoint.isDelegate()) {
            return DecorationHelper.peek().getNextDelegate(injectionPoint, creationalContext);
        } else {
            Bean<?> resolvedBean = getBean(new ResolvableBuilder(injectionPoint, this).create());
            return getReference(injectionPoint, resolvedBean, creationalContext);
        }
    }

    public <T> Bean<T> getBean(Resolvable resolvable) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Bean<T> bean = cast(resolve(beanResolver.resolve(resolvable, true)));
        if (bean == null) {
            throw new UnsatisfiedResolutionException(UNRESOLVABLE_ELEMENT, resolvable);
        }

        if (isNormalScoped(bean) && !Beans.isBeanProxyable(bean)) {
            throw Proxies.getUnproxyableTypesException(bean);
        }
        return bean;
    }

    public boolean isNormalScoped(Bean<?> bean) {
        return getMetaAnnotationStore().getScopeModel(bean.getScope()).isNormal();
    }

    public Set<Bean<?>> getBeans(String name) {
        return nameBasedResolver.resolve(name);
    }

    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        checkResolveDecoratorsArguments(types);
        // TODO Fix this cast and make the resolver return a list
        return new ArrayList<Decorator<?>>(decoratorResolver.resolve(new ResolvableBuilder(this).addTypes(types).addQualifiers(qualifiers).create(), isCacheable(qualifiers)));
    }

    public List<Decorator<?>> resolveDecorators(Set<Type> types, Set<Annotation> qualifiers) {
        checkResolveDecoratorsArguments(types);
        // TODO Fix this cast and make the resolver return a list
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        return new ArrayList<Decorator<?>>(decoratorResolver.resolve(new ResolvableBuilder(this).addTypes(types).addQualifiers(qualifiers).create(), true));
    }

    private void checkResolveDecoratorsArguments(Set<Type> types) {
        if (types.isEmpty()) {
            throw new IllegalArgumentException(NO_DECORATOR_TYPES);
        }
    }

    /**
     * Resolves a list of interceptors based on interception type and interceptor
     * bindings
     *
     * @param type                The interception type to resolve
     * @param interceptorBindings The binding types to match
     * @return A list of matching interceptors
     * @see javax.enterprise.inject.spi.BeanManager#resolveInterceptors(javax.enterprise.inject.spi.InterceptionType,
     *      java.lang.annotation.Annotation...)
     */
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        if (interceptorBindings.length == 0) {
            throw new IllegalArgumentException(INTERCEPTOR_BINDINGS_EMPTY);
        }
        Set<InterceptorBindingType> interceptorBindingTypes = new HashSet<InterceptorBindingType>();
        for (Annotation annotation : interceptorBindings) {
            if (!isInterceptorBinding(annotation.annotationType())) {
                throw new IllegalArgumentException(NOT_INTERCEPTOR_BINDING_TYPE, annotation);
            }
            InterceptorBindingType interceptorBindingType = new InterceptorBindingType(this, annotation);
            if (interceptorBindingTypes.contains(interceptorBindingType)) {
                throw new IllegalArgumentException(DUPLICATE_INTERCEPTOR_BINDING, annotation);
            }
            interceptorBindingTypes.add(interceptorBindingType);
        }

        return resolveInterceptors(type, flattenInterceptorBindings(interceptorBindingTypes));
    }

    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Set<InterceptorBindingType> flattenedInterceptorBindings) {
        Set<Annotation> interceptorBindingAnnotations = InterceptorBindingType.unwrap(flattenedInterceptorBindings);

        InterceptorResolvable interceptorResolvable = new InterceptorResolvableBuilder(Object.class, this)
                .setInterceptionType(type)
                .addQualifiers(interceptorBindingAnnotations)
                .create();
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        return new ArrayList<Interceptor<?>>(interceptorResolver.resolve(interceptorResolvable, isCacheable(interceptorBindingAnnotations)));
    }

    /**
     * Get the web bean resolver. For internal use
     *
     * @return The resolver
     */
    public TypeSafeBeanResolver<Bean<?>> getBeanResolver() {
        return beanResolver;
    }

    /**
     * Get the decorator resolver. For internal use
     *
     * @return The resolver
     */
    public TypeSafeResolver<Resolvable, Decorator<?>> getDecoratorResolver() {
        return decoratorResolver;
    }

    /**
     * Get the observer notifier for accessible observer methods. For internal use
     *
     * @return The {@link ObserverNotifier}
     */
    public ObserverNotifier getAccessibleObserverNotifier() {
        return accessibleObserverNotifier;
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Manager\n");
        buffer.append("Enabled alternatives: " + getEnabled().getAlternativeClasses() + " " + getEnabled().getAlternativeStereotypes() + "\n");
        buffer.append("Registered contexts: " + contexts.keySet() + "\n");
        buffer.append("Registered beans: " + getBeans().size() + "\n");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanManagerImpl) {
            BeanManagerImpl that = (BeanManagerImpl) obj;
            return this.getId().equals(that.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public BeanManagerImpl createActivity() {
        BeanManagerImpl childActivity = newChildActivityManager(this);
        childActivities.add(childActivity);
        Container.instance(contextId).addActivity(childActivity);
        return childActivity;
    }

    public BeanManagerImpl setCurrent(Class<? extends Annotation> scopeType) {
        if (!getMetaAnnotationStore().getScopeModel(scopeType).isNormal()) {
            throw new IllegalArgumentException(NON_NORMAL_SCOPE, scopeType);
        }
        currentActivities.add(new CurrentActivity(getContext(scopeType), this));
        return this;
    }

    public BeanManagerImpl getCurrent() {
        CurrentActivity activeCurrentActivity = null;
        for (CurrentActivity currentActivity : currentActivities) {
            if (currentActivity.getContext().isActive()) {
                if (activeCurrentActivity == null)
                    activeCurrentActivity = currentActivity;
                else
                    throw new IllegalStateException(TOO_MANY_ACTIVITIES, currentActivities);
            }
        }
        if (activeCurrentActivity == null) {
            return this;
        } else {
            return activeCurrentActivity.getManager();
        }
    }

    public ServiceRegistry getServices() {
        return services;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    @Deprecated // should nto be used anymore
    public Map<Contextual<?>, Contextual<?>> getSpecializedBeans() {
        return closure.getSpecialized();
    }

    // Serialization

    protected Object readResolve() {
        return Container.instance(contextId).activityManager(id);
    }

    public ClientProxyProvider getClientProxyProvider() {
        return clientProxyProvider;
    }

    protected Map<Class<? extends Annotation>, List<Context>> getContexts() {
        return contexts;
    }

    /**
     * @return the namespaces
     */
    protected List<String> getNamespaces() {
        return namespaces;
    }

    public Iterable<String> getAccessibleNamespaces() {
        // TODO Cache this
        return createDynamicAccessibleIterable(new NamespaceTransform());
    }

    private Set<CurrentActivity> getCurrentActivities() {
        return currentActivities;
    }

    public String getId() {
        return id;
    }

    public AtomicInteger getChildIds() {
        return childIds;
    }

    public List<ObserverMethod<?>> getObservers() {
        return observers;
    }

    public Namespace getRootNamespace() {
        // TODO I don't like this lazy init
        if (rootNamespace == null) {
            rootNamespace = new Namespace(createDynamicAccessibleIterable(new NamespaceTransform()));
        }
        return rootNamespace;
    }

    public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type) {
        InjectionTarget<T> injectionTarget = new SimpleInjectionTarget<T>(loadClass(type), this);
        getServices().get(InjectionTargetValidator.class).addInjectionTarget(injectionTarget);
        return injectionTarget;
    }

    private <T> InjectionTarget<T> createMessageDrivenInjectionTarget(AnnotatedType<T> type) {
        return new MessageDrivenInjectionTarget<T>(loadClass(type), this);
    }

    private <T> WeldClass<T> loadClass(AnnotatedType<T> type) {
        return getServices().get(ClassTransformer.class).loadClass(type);
    }

    public <T> InjectionTarget<T> createInjectionTarget(EjbDescriptor<T> descriptor) {
        if (descriptor.isMessageDriven()) {

            InjectionTarget<T> injectionTarget = createMessageDrivenInjectionTarget(createAnnotatedType(descriptor.getBeanClass()));
            getServices().get(InjectionTargetValidator.class).addInjectionTarget(injectionTarget);
            return injectionTarget;
        } else {
            InjectionTarget<T> injectionTarget = getBean(descriptor).getInjectionTarget();
            getServices().get(InjectionTargetValidator.class).addInjectionTarget(injectionTarget);
            return injectionTarget;
        }
    }

    public <X> Bean<? extends X> getMostSpecializedBean(Bean<X> bean) {
        //noinspection unchecked
        return (Bean<? extends X>) closure.mostSpecialized(bean);
    }

    public void validate(InjectionPoint ij) {
        try {
            getServices().get(Validator.class).validateInjectionPoint(ij, this);
        } catch (DeploymentException e) {
            throw new InjectionException(e.getLocalizedMessage(), e.getCause());
        }
    }

    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
        InterceptorBindingModel<? extends Annotation> model = getMetaAnnotationStore().getInterceptorBindingModel(bindingType);
        if (model.isValid()) {
            return model.getMetaAnnotations();
        } else {
            throw new IllegalArgumentException(NOT_INTERCEPTOR_BINDING_TYPE, bindingType);
        }
    }

    public Bean<?> getPassivationCapableBean(String id) {
        return getServices().get(ContextualStore.class).<Bean<Object>, Object>getContextual(id);
    }

    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
        final StereotypeModel<? extends Annotation> model = getMetaAnnotationStore().getStereotype(stereotype);
        if (model.isValid()) {
            return model.getMetaAnnotations();
        } else {
            throw new IllegalArgumentException(NOT_STEREOTYPE, stereotype);
        }
    }

    public boolean isQualifier(Class<? extends Annotation> annotationType) {
        return getMetaAnnotationStore().getBindingTypeModel(annotationType).isValid();
    }

    public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
        return getMetaAnnotationStore().getInterceptorBindingModel(annotationType).isValid();
    }

    public boolean isNormalScope(Class<? extends Annotation> annotationType) {
        ScopeModel<?> scope = getMetaAnnotationStore().getScopeModel(annotationType);
        return scope.isValid() && scope.isNormal();
    }

    public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
        ScopeModel<?> scope = getMetaAnnotationStore().getScopeModel(annotationType);
        return scope.isValid() && scope.isPassivating();
    }

    public boolean isScope(Class<? extends Annotation> annotationType) {
        return getMetaAnnotationStore().getScopeModel(annotationType).isValid();
    }

    public boolean isStereotype(Class<? extends Annotation> annotationType) {
        return getMetaAnnotationStore().getStereotype(annotationType).isValid();
    }

    public MetaAnnotationStore getMetaAnnotationStore() {
        return getServices().get(MetaAnnotationStore.class);
    }

    public ELResolver getELResolver() {
        return weldELResolver;
    }

    public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
        return new WeldExpressionFactory(expressionFactory);
    }

    public <T> WeldCreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        return new CreationalContextImpl<T>(contextual);
    }

    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
        return getServices().get(ClassTransformer.class).loadClass(type);
    }

    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        Set<Bean<? extends X>> resolvedBeans = beanResolver.resolve(beans);
        if (resolvedBeans.size() == 1) {
            return resolvedBeans.iterator().next();
        } else if (resolvedBeans.size() == 0) {
            return null;
        } else {
            throw new AmbiguousResolutionException(AMBIGUOUS_BEANS_FOR_DEPENDENCY, beans);
        }
    }

    public <T> EjbDescriptor<T> getEjbDescriptor(String beanName) {
        return getServices().get(EjbDescriptors.class).get(beanName);
    }

    public <T> SessionBean<T> getBean(EjbDescriptor<T> descriptor) {
        return cast(getEnterpriseBeans().get(descriptor));
    }

    public void cleanup() {
        services.cleanup();
        this.accessibleManagers.clear();
        this.beanResolver.clear();
        this.beans.clear();
        this.childActivities.clear();
        this.clientProxyProvider.clear();
        this.contexts.clear();
        this.currentActivities.clear();
        this.decoratorResolver.clear();
        this.decorators.clear();
        this.enterpriseBeans.clear();
        this.interceptorResolver.clear();
        this.interceptors.clear();
        this.nameBasedResolver.clear();
        this.namespaces.clear();
        this.accessibleObserverNotifier.clear();
        this.observers.clear();
        this.closure.destroy();
    }

    public Map<Class<?>, InterceptionModel<ClassMetadata<?>, ?>> getInterceptorModelRegistry() {
        return interceptorModelRegistry;
    }

    public MetadataCachingReader getInterceptorMetadataReader() {
        return interceptorMetadataReader;
    }

    public <X> InjectionTarget<X> fireProcessInjectionTarget(AnnotatedType<X> annotatedType) {
        return AbstractProcessInjectionTarget.fire(this, annotatedType, createInjectionTarget(annotatedType));
    }

    public Set<InterceptorBindingType> extractAndFlattenInterceptorBindings(Iterable<Annotation> annotations) {
        return flattenInterceptorBindings(extractInterceptorBindings(annotations));
    }

    public Set<InterceptorBindingType> extractInterceptorBindings(Iterable<Annotation> annotations) {
        Set<InterceptorBindingType> foundInterceptionBindingTypes = new HashSet<InterceptorBindingType>();
        for (Annotation annotation : annotations) {
            if (isInterceptorBinding(annotation.annotationType())) {
                foundInterceptionBindingTypes.add(new InterceptorBindingType(this, annotation));
            }
        }
        return foundInterceptionBindingTypes;
    }

    public Set<QualifierInstance> extractInterceptorBindingsForQualifierInstance(Iterable<QualifierInstance> annotations) {
        Set<QualifierInstance> foundInterceptionBindingTypes = new HashSet<QualifierInstance>();
        for (QualifierInstance annotation : annotations) {
            if (isInterceptorBinding(annotation.getAnnotationClass())) {
                foundInterceptionBindingTypes.add(annotation);
            }
        }
        return foundInterceptionBindingTypes;
    }
    /**
     * Extracts the complete set of interception bindings from a given set of
     * interceptorBindingTypes.
     *
     * @param interceptorBindingTypes
     * @return
     */
    public Set<InterceptorBindingType> flattenInterceptorBindings(Set<InterceptorBindingType> interceptorBindingTypes) {
        MetaAnnotationStore metaAnnotationStore = getMetaAnnotationStore();

        Set<InterceptorBindingType> foundInterceptionBindingTypes = new HashSet<InterceptorBindingType>();
        for (InterceptorBindingType interceptorBindingType : interceptorBindingTypes) {
            foundInterceptionBindingTypes.add(interceptorBindingType);

            InterceptorBindingModel<? extends Annotation> interceptorBindingModel = metaAnnotationStore.getInterceptorBindingModel(interceptorBindingType.annotationType());
            foundInterceptionBindingTypes.addAll(flattenInterceptorBindings(extractInterceptorBindings(interceptorBindingModel.getInheritedInterceptionBindingTypes())));
        }
        return foundInterceptionBindingTypes;
    }

    private static class InstanceInjectionPoint implements InjectionPoint, Serializable {

        private static final InjectionPoint INSTANCE = new InstanceInjectionPoint();

        private transient Type type;
        private transient Set<Annotation> qualifiers;

        public Type getType() {
            if (type == null) {
                this.type = new TypeLiteral<Instance<Object>>() {
                }.getType();
            }
            return type;
        }

        public Set<Annotation> getQualifiers() {
            if (qualifiers == null) {
                this.qualifiers = Arrays2.asSet(DefaultLiteral.INSTANCE, AnyLiteral.INSTANCE);
            }
            return qualifiers;
        }

        public Bean<?> getBean() {
            return null;
        }

        public Member getMember() {
            return null;
        }

        public Annotated getAnnotated() {
            return null;
        }

        public boolean isDelegate() {
            return false;
        }

        public boolean isTransient() {
            return false;
        }

    }

    public Instance<Object> instance() {
        return InstanceImpl.of(InstanceInjectionPoint.INSTANCE, createCreationalContext(null), this);
    }

}
