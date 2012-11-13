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

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanManagerMessage.AMBIGUOUS_BEANS_FOR_DEPENDENCY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.CONTEXT_NOT_ACTIVE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_ACTIVE_CONTEXTS;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INCORRECT_PRODUCER_MEMBER;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_BINDINGS_EMPTY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NON_NORMAL_SCOPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NOT_INTERCEPTOR_BINDING_TYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NOT_STEREOTYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NO_DECORATOR_TYPES;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NO_INSTANCE_OF_EXTENSION;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_BEAN_ARGUMENT;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_BEAN_TYPE_ARGUMENT;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_CREATIONAL_CONTEXT_ARGUMENT;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NULL_DECLARING_BEAN;
import static org.jboss.weld.logging.messages.BeanManagerMessage.SPECIFIED_TYPE_NOT_BEAN_TYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.TOO_MANY_ACTIVITIES;
import static org.jboss.weld.logging.messages.BeanManagerMessage.UNRESOLVABLE_ELEMENT;
import static org.jboss.weld.logging.messages.BootstrapMessage.FOUND_BEAN;
import static org.jboss.weld.manager.BeanManagers.buildAccessibleClosure;
import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.jboss.weld.util.reflection.Reflections.isCacheable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.AnnotatedTypeValidator;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.SyntheticBeanFactory;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bean.builtin.InstanceImpl;
import org.jboss.weld.bean.proxy.ClientProxyProvider;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEvents;
import org.jboss.weld.context.ContextNotActiveException;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.context.PassivatingContextWrapper;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.el.Namespace;
import org.jboss.weld.el.WeldELResolver;
import org.jboss.weld.el.WeldExpressionFactory;
import org.jboss.weld.event.GlobalObserverNotifierService;
import org.jboss.weld.event.ObserverNotifier;
import org.jboss.weld.exceptions.AmbiguousResolutionException;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.InjectionException;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.InferingFieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.InferingParameterInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ParameterInjectionPointAttributes;
import org.jboss.weld.injection.producer.AbstractInjectionTarget;
import org.jboss.weld.injection.producer.DecoratorInjectionTarget;
import org.jboss.weld.injection.producer.DefaultInjectionTarget;
import org.jboss.weld.injection.producer.InjectionTargetInitializationContext;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.injection.producer.ProducerFieldProducer;
import org.jboss.weld.injection.producer.ProducerMethodProducer;
import org.jboss.weld.injection.producer.ejb.SessionBeanInjectionTarget;
import org.jboss.weld.interceptor.reader.cache.DefaultMetadataCachingReader;
import org.jboss.weld.interceptor.reader.cache.MetadataCachingReader;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.ScopeModel;
import org.jboss.weld.metadata.cache.StereotypeModel;
import org.jboss.weld.resolution.BeanTypeAssignabilityRules;
import org.jboss.weld.resolution.DecoratorResolvableBuilder;
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
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.MemberTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.Interceptors;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.collections.IterableToIteratorFunction;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

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
 * @author Jozef Hartinger
 */
public class BeanManagerImpl implements WeldManager, Serializable {

    private static final long serialVersionUID = 3021562879133838561L;

    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

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
    private transient volatile ModuleEnablement enabled;
    private final transient Set<CurrentActivity> currentActivities;


    /*
    * Activity scoped services
    * *************************
    */

    /* These services are scoped to this activity only, but use data
    * structures that are transitive accessible from other bean deployment
    * archives
    */
    private final transient TypeSafeBeanResolver beanResolver;
    private final transient TypeSafeDecoratorResolver decoratorResolver;
    private final transient TypeSafeInterceptorResolver interceptorResolver;
    private final transient NameBasedResolver nameBasedResolver;
    private final transient ELResolver weldELResolver;
    private transient Namespace rootNamespace;

    /*
     * Lenient instances do not perform event type checking - this is required for firing container lifecycle events.
     * Strict instances do performe event type checking and are used for firing application an extension events.
     */
    private final transient ObserverNotifier accessibleLenientObserverNotifier;
    private final transient ObserverNotifier globalLenientObserverNotifier;
    private final transient ObserverNotifier globalStrictObserverNotifier;

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

    /**
     * Interception model
     */
    private final transient ConcurrentMap<Class<?>, InterceptionModel<ClassMetadata<?>, ?>> interceptorModelRegistry = new ConcurrentHashMap<Class<?>, InterceptionModel<ClassMetadata<?>, ?>>();
    private final transient MetadataCachingReader interceptorMetadataReader = new DefaultMetadataCachingReader();

    private final transient ContainerLifecycleEvents containerLifecycleEvents;

    /**
     * Create a new, root, manager
     *
     * @param serviceRegistry
     * @return
     */
    public static BeanManagerImpl newRootManager(String id, ServiceRegistry serviceRegistry) {
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
                new ClientProxyProvider(),
                contexts,
                new CopyOnWriteArraySet<CurrentActivity>(),
                ModuleEnablement.EMPTY_ENABLEMENT,
                id,
                new AtomicInteger());
    }

    public static BeanManagerImpl newManager(BeanManagerImpl rootManager, String id, ServiceRegistry services) {
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
                ModuleEnablement.EMPTY_ENABLEMENT,
                id,
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
            ModuleEnablement enabled,
            String id,
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
        this.childIds = new AtomicInteger();

        // Set up the structure to store accessible managers in
        this.accessibleManagers = new HashSet<BeanManagerImpl>();

        // TODO Currently we build the accessible bean list on the fly, we need to set it in stone once bootstrap is finished...
        Transform<Bean<?>> beanTransform = new BeanTransform(this);
        this.beanResolver = new TypeSafeBeanResolver(this, createDynamicAccessibleIterable(beanTransform));
        this.decoratorResolver = new TypeSafeDecoratorResolver(this, createDynamicAccessibleIterable(DecoratorTransform.INSTANCE));
        this.interceptorResolver = new TypeSafeInterceptorResolver(this, createDynamicAccessibleIterable(InterceptorTransform.INSTANCE));
        this.nameBasedResolver = new NameBasedResolver(this, createDynamicAccessibleIterable(beanTransform));
        this.weldELResolver = new WeldELResolver(this);
        this.childActivities = new CopyOnWriteArraySet<BeanManagerImpl>();

        TypeSafeObserverResolver accessibleObserverResolver = new TypeSafeObserverResolver(getServices().get(MetaAnnotationStore.class), createDynamicAccessibleIterable(ObserverMethodTransform.INSTANCE));
        this.accessibleLenientObserverNotifier = ObserverNotifier.of(accessibleObserverResolver, getServices(), false);
        GlobalObserverNotifierService globalObserverNotifierService = services.get(GlobalObserverNotifierService.class);
        this.globalLenientObserverNotifier = globalObserverNotifierService.getGlobalLenientObserverNotifier();
        this.globalStrictObserverNotifier = globalObserverNotifierService.getGlobalStrictObserverNotifier();
        globalObserverNotifierService.registerBeanManager(this);
        this.containerLifecycleEvents = serviceRegistry.get(ContainerLifecycleEvents.class);
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
        accessibleLenientObserverNotifier.clear();
    }

    public HashSet<BeanManagerImpl> getAccessibleManagers() {
        return accessibleManagers;
    }

    public void addBean(Bean<?> bean) {
        addBean(bean, beans, transitiveBeans);
    }

    /**
     * Optimization which modifies CopyOnWrite structures only once instead of once for every bean.
     * @param beans
     */
    public void addBeans(Collection<? extends Bean<?>> beans) {
        List<Bean<?>> beanList = new ArrayList<Bean<?>>(beans.size());
        List<Bean<?>> transitiveBeans = new ArrayList<Bean<?>>(beans.size());
        for (Bean<?> bean : beans) {
            addBean(bean, beanList, transitiveBeans);
        }
        // optimize so that we do not modify CopyOnWriteLists for each Bean
        this.beans.addAll(beanList);
        this.transitiveBeans.addAll(transitiveBeans);
        for (BeanManagerImpl childActivity : childActivities) {
            childActivity.addBeans(beanList);
        }
    }

    private void addBean(Bean<?> bean, List<Bean<?>> beanList, List<Bean<?>> transitiveBeans) {
        if (beanSet.add(bean)) {
            log.debug(FOUND_BEAN, bean);
            beanList.add(bean);
            if (bean instanceof SessionBean) {
                SessionBean<?> enterpriseBean = (SessionBean<?>) bean;
                enterpriseBeans.put(enterpriseBean.getEjbDescriptor(), enterpriseBean);
            }
            if (bean instanceof PassivationCapable) {
                Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
            }
            registerBeanNamespace(bean);
            // New beans (except for SessionBeans) and most built in beans aren't resolvable transtively
            if (bean instanceof ExtensionBean || bean instanceof SessionBean || (!(bean instanceof NewBean) && !(bean instanceof AbstractBuiltInBean<?>))) {
                transitiveBeans.add(bean);
            }
        }
    }

    public void addDecorator(Decorator<?> bean) {
        decorators.add(bean);
        getServices().get(ContextualStore.class).putIfAbsent(bean);
        decoratorResolver.clear();
    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... bindings) {
        return globalStrictObserverNotifier.resolveObserverMethods(event, bindings);
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
    public ModuleEnablement getEnabled() {
        return enabled;
    }

    public void setEnabled(ModuleEnablement enabled) {
        this.enabled = enabled;
    }

    public boolean isBeanEnabled(Bean<?> bean) {
        return Beans.isBeanEnabled(bean, getEnabled());
    }

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        Resolvable resolvable = new ResolvableBuilder(beanType, this).addQualifiers(qualifiers).create();
        return beanResolver.resolve(resolvable, isCacheable(qualifiers));
    }

    public Set<Bean<?>> getBeans(Type beanType, Set<Annotation> qualifiers) {
        return beanResolver.resolve(new ResolvableBuilder(beanType, this).addQualifiers(qualifiers).create(), isCacheable(qualifiers));
    }

    public Set<Bean<?>> getBeans(InjectionPoint injectionPoint) {
        boolean registerInjectionPoint = isRegisterableInjectionPoint(injectionPoint);
        CurrentInjectionPoint currentInjectionPoint = null;
        if (registerInjectionPoint) {
            currentInjectionPoint = services.get(CurrentInjectionPoint.class);
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
        if (isPassivatingScope(scope)) {
            context = PassivatingContextWrapper.wrap(context, services.get(ContextualStore.class));
        }
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
    @Override
    public void fireEvent(Object event, Annotation... qualifiers) {
        globalStrictObserverNotifier.fireEvent(event, qualifiers);
    }

    /**
     * Gets an active context of the given scope. Throws an exception if there
     * are no active contexts found or if there are too many matches
     *
     * @throws IllegalStateException if there are multiple active scopes for a given context
     * @param scopeType The scope to match
     * @return A single active context of the given scope
     * @see javax.enterprise.inject.spi.BeanManager#getContext(java.lang.Class)
     */
    @Override
    public Context getContext(Class<? extends Annotation> scopeType) {
        Context activeContext = internalGetContext(scopeType);
        if (activeContext == null) {
            throw new ContextNotActiveException(CONTEXT_NOT_ACTIVE, scopeType.getName());
        }
        return activeContext;
    }

    /**
     * Indicates whether there is an active context for a given scope.
     *
     * @throws IllegalStateException if there are multiple active scopes for a given context
     * @param scopeType
     * @return true if there is an active context for a given scope, false otherwise
     */
    public boolean isContextActive(Class<? extends Annotation> scopeType) {
        return internalGetContext(scopeType) != null;
    }

    private Context internalGetContext(Class<? extends Annotation> scopeType) {
        Context activeContext = null;
        final List<Context> ctx = contexts.get(scopeType);
        if (ctx == null) {
            return null;
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
        return activeContext;
    }

    public Object getReference(Bean<?> bean, Type requestedType, CreationalContext<?> creationalContext, boolean noProxy) {
        if (creationalContext instanceof WeldCreationalContext<?>) {
            creationalContext = ((WeldCreationalContext<?>) creationalContext).getCreationalContext(bean);
        }
        if (!noProxy && isProxyRequired(bean)) {
            if (creationalContext != null || getContext(bean.getScope()).get(bean) != null) {
                if (requestedType == null) {
                    return clientProxyProvider.getClientProxy(bean);
                } else {
                    return clientProxyProvider.getClientProxy(bean, requestedType);
                }
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
        } else if (isNormalScope(bean.getScope())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getReference(Bean<?> bean, Type requestedType, CreationalContext<?> creationalContext) {
        if (bean == null) {
            throw new IllegalArgumentException(NULL_BEAN_ARGUMENT);
        }
        if (requestedType == null) {
            throw new IllegalArgumentException(NULL_BEAN_TYPE_ARGUMENT);
        }
        if (creationalContext == null) {
            throw new IllegalArgumentException(NULL_CREATIONAL_CONTEXT_ARGUMENT);
        }
        if (!BeanTypeAssignabilityRules.instance().matches(requestedType, bean.getTypes())) {
            throw new IllegalArgumentException(SPECIFIED_TYPE_NOT_BEAN_TYPE, requestedType, bean);
        }
        return getReference(bean, requestedType, creationalContext, false);
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
        boolean registerInjectionPoint = isRegisterableInjectionPoint(injectionPoint);
        boolean delegateInjectionPoint = injectionPoint != null && injectionPoint.isDelegate();

        CurrentInjectionPoint currentInjectionPoint = null;
        if (registerInjectionPoint) {
            currentInjectionPoint = services.get(CurrentInjectionPoint.class);
            currentInjectionPoint.push(injectionPoint);
        }
        try {
            Type requestedType = null;
            if (injectionPoint != null) {
                requestedType = injectionPoint.getType();
            }
            // TODO Can we move this logic to getReference?
            if (creationalContext instanceof WeldCreationalContext<?>) {
                WeldCreationalContext<?> wbCreationalContext = (WeldCreationalContext<?>) creationalContext;
                final Object incompleteInstance = wbCreationalContext.getIncompleteInstance(resolvedBean);
                if (incompleteInstance != null) {
                    return incompleteInstance;
                } else {
                    return getReference(resolvedBean, requestedType, wbCreationalContext, delegateInjectionPoint);
                }
            } else {
                return getReference(resolvedBean, requestedType, creationalContext, delegateInjectionPoint);
            }
        } finally {
            if (registerInjectionPoint) {
                currentInjectionPoint.pop();
            }
        }
    }

    @Override
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

        if (isNormalScope(bean.getScope()) && !Beans.isBeanProxyable(bean)) {
            throw Proxies.getUnproxyableTypesException(bean);
        }
        return bean;
    }

    @Override
    public Set<Bean<?>> getBeans(String name) {
        return nameBasedResolver.resolve(name);
    }

    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        checkResolveDecoratorsArguments(types);
        return decoratorResolver.resolve(new DecoratorResolvableBuilder(this).addTypes(types).addQualifiers(qualifiers).create(), isCacheable(qualifiers));
    }

    public List<Decorator<?>> resolveDecorators(Set<Type> types, Set<Annotation> qualifiers) {
        checkResolveDecoratorsArguments(types);
        return decoratorResolver.resolve(new DecoratorResolvableBuilder(this).addTypes(types).addQualifiers(qualifiers).create(), true);
    }

    private void checkResolveDecoratorsArguments(Set<Type> types) {
        if (types.isEmpty()) {
            throw new IllegalArgumentException(NO_DECORATOR_TYPES);
        }
    }

    /**
     * Resolves a list of interceptors based on interception type and interceptor
     * bindings. Transitive interceptor bindings of the interceptor bindings passed
     * as a parameter are considered in the resolution process.
     *
     * @param type                The interception type to resolve
     * @param interceptorBindings The binding types to match
     * @return A list of matching interceptors
     * @see javax.enterprise.inject.spi.BeanManager#resolveInterceptors(javax.enterprise.inject.spi.InterceptionType,
     *      java.lang.annotation.Annotation[])
     */
    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        if (interceptorBindings.length == 0) {
            throw new IllegalArgumentException(INTERCEPTOR_BINDINGS_EMPTY);
        }
        for (Annotation annotation : interceptorBindings) {
            if (!isInterceptorBinding(annotation.annotationType())) {
               throw new IllegalArgumentException(NOT_INTERCEPTOR_BINDING_TYPE, annotation);
            }
        }
        Set<Annotation> flattenedInterceptorBindings = Interceptors.flattenInterceptorBindings(this, Arrays.asList(interceptorBindings), true, true);
        return resolveInterceptors(type, flattenedInterceptorBindings);
    }

    /**
     * Resolves a list of interceptors based on interception type and interceptor
     * bindings. Transitive interceptor bindings of the interceptor bindings passed
     * as a parameter are NOT considered in the resolution process. Therefore, the caller
     * is responsible for filtering of transitive interceptor bindings in order to comply
     * with interceptor binding inheritance and overriding (See JSR-346 9.5.2).
     * This is a Weld-specific method.
     *
     * @param type                The interception type to resolve
     * @param interceptorBindings The binding types to match
     * @return A list of matching interceptors
     */
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Collection<Annotation> interceptorBindings) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        InterceptorResolvable interceptorResolvable = new InterceptorResolvableBuilder(Object.class, this)
        .setInterceptionType(type)
        .addQualifiers(interceptorBindings)
        .create();
        return interceptorResolver.resolve(interceptorResolvable, isCacheable(interceptorBindings));
    }

    /**
     * Get the web bean resolver. For internal use
     *
     * @return The resolver
     */
    public TypeSafeBeanResolver getBeanResolver() {
        return beanResolver;
    }

    /**
     * Get the decorator resolver. For internal use
     *
     * @return The resolver
     */
    public TypeSafeDecoratorResolver getDecoratorResolver() {
        return decoratorResolver;
    }

    /**
     * Get the lenient observer notifier for accessible observer methods. Should never be exposed to an application.
     *
     * @return The {@link ObserverNotifier}
     */
    public ObserverNotifier getAccessibleLenientObserverNotifier() {
        return accessibleLenientObserverNotifier;
    }

    /**
     * Get the lenient global observer notifier. Should never be exposed to an application.
     *
     * @return The {@link ObserverNotifier}
     */
    public ObserverNotifier getGlobalLenientObserverNotifier() {
        return globalLenientObserverNotifier;
    }

    /**
     * Get the Strict global observer notifier. This one should be used for firing application / extension events.
     *
     * @return The {@link ObserverNotifier}
     */
    public ObserverNotifier getGlobalStrictObserverNotifier() {
        return globalStrictObserverNotifier;
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
        buffer.append("Enabled alternatives: " + getEnabled().getAlternatives() + "\n");
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
        Container.instance().addActivity(childActivity);
        return childActivity;
    }

    public BeanManagerImpl setCurrent(Class<? extends Annotation> scopeType) {
        if (!getServices().get(MetaAnnotationStore.class).getScopeModel(scopeType).isNormal()) {
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

    // Serialization

    protected Object readResolve() {
        return Container.instance().activityManager(id);
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

    @Override
    public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type) {
        return createInjectionTarget(type, null);
    }

    public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type, Bean<T> bean) {
        AnnotatedTypeValidator.validateAnnotatedType(type);
        try {
            EnhancedAnnotatedType<T> enhancedType = getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(type);
            InjectionTarget<T> injectionTarget = internalCreateInjectionTarget(enhancedType, bean);

            getServices().get(InjectionTargetService.class).validateProducer(injectionTarget);
            return injectionTarget;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public <T> AbstractInjectionTarget<T> internalCreateInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean) {
        AbstractInjectionTarget<T> injectionTarget = null;
        if (bean instanceof DecoratorImpl<?> || type.isAnnotationPresent(javax.decorator.Decorator.class) || type.isAbstract()) {
            // TODO if we get an abstract class, we assume that it is a decorator class - is this good?
            injectionTarget = new DecoratorInjectionTarget<T>(type, bean, this);
        } else if (bean instanceof SessionBean<?>) {
            injectionTarget = new SessionBeanInjectionTarget<T>(type, (SessionBean<T>) bean, this);
        } else {
            injectionTarget = new DefaultInjectionTarget<T>(type, bean, this);
        }
        /*
         * Every InjectionTarget, regardless whether it's used within Weld's Bean implementation or requested from extension
         * has to be initialized after beans (interceptors) are deployed.
         */
        getServices().get(InjectionTargetService.class).addInjectionTargetToBeInitialized(new InjectionTargetInitializationContext<T>(type, injectionTarget));
        return injectionTarget;
    }

    public <T> InjectionTarget<T> createInjectionTarget(EjbDescriptor<T> descriptor) {
        if (descriptor.isMessageDriven()) {
            throw new UnsupportedOperationException();
        } else {
            InjectionTarget<T> injectionTarget = getBean(descriptor).getProducer();
            getServices().get(InjectionTargetService.class).validateProducer(injectionTarget);
            return injectionTarget;
        }
    }

    /**
     * Creates a new {@link Producer} implementation for a given field. This method is required by the specification.
     */
    @Override
    public <X> Producer<?> createProducer(AnnotatedField<? super X> field, Bean<X> declaringBean) {
        if (declaringBean == null && !field.isStatic()) {
            throw new IllegalArgumentException(NULL_DECLARING_BEAN, field);
        }
        AnnotatedTypeValidator.validateAnnotatedMember(field);
        try {
            Producer<?> producer = createProducer(field, null, declaringBean, null);
            getServices().get(InjectionTargetService.class).validateProducer(producer);
            return producer;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public <S, X extends S , T> Producer<T> createProducer(final AnnotatedField<S> field, DisposalMethod<X, T> disposalMethod, final Bean<X> declaringBean, final Bean<T> bean) {
        EnhancedAnnotatedField<T, S> enhancedField = getServices().get(MemberTransformer.class).loadEnhancedMember(field);
        return new ProducerFieldProducer<X, T>(enhancedField, disposalMethod) {

            @Override
            public AnnotatedField<? super X> getAnnotated() {
                return field;
            }

            @Override
            public BeanManagerImpl getBeanManager() {
                return BeanManagerImpl.this;
            }

            @Override
            public Bean<X> getDeclaringBean() {
                return declaringBean;
            }

            @Override
            public Bean<T> getBean() {
                return bean;
            }
        };
    }

    /**
     * Creates a new {@link Producer} implementation for a given method. This method is required by the specification.
     */
    @Override
    public <X> Producer<?> createProducer(AnnotatedMethod<? super X> method, Bean<X> declaringBean) {
        if (declaringBean == null && !method.isStatic()) {
            throw new IllegalArgumentException(NULL_DECLARING_BEAN, method);
        }
        AnnotatedTypeValidator.validateAnnotatedMember(method);
        try {
            Producer<?> producer = createProducer(method, null, declaringBean, null);
            getServices().get(InjectionTargetService.class).validateProducer(producer);
            return producer;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public <S, X extends S, T> Producer<T> createProducer(final AnnotatedMethod<S> method, DisposalMethod<X, T> disposalMethod, final Bean<X> declaringBean, final Bean<T> bean) {
        EnhancedAnnotatedMethod<T, S> enhancedMethod = getServices().get(MemberTransformer.class).loadEnhancedMember(method);
        return new ProducerMethodProducer<X, T>(enhancedMethod, disposalMethod) {

            @Override
            public AnnotatedMethod<? super X> getAnnotated() {
                return method;
            }

            @Override
            public BeanManagerImpl getBeanManager() {
                return BeanManagerImpl.this;
            }

            @Override
            public Bean<X> getDeclaringBean() {
                return declaringBean;
            }

            @Override
            public Bean<T> getBean() {
                return bean;
            }
        };
    }

    @Override
    public void validate(InjectionPoint ij) {
        try {
            getServices().get(Validator.class).validateInjectionPoint(ij, this);
        } catch (DeploymentException e) {
            throw new InjectionException(e.getLocalizedMessage(), e.getCause());
        }
    }

    @Override
    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
        InterceptorBindingModel<? extends Annotation> model = getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(bindingType);
        if (model.isValid()) {
            return model.getMetaAnnotations();
        } else {
            throw new IllegalArgumentException(NOT_INTERCEPTOR_BINDING_TYPE, bindingType);
        }
    }

    @Override
    public Bean<?> getPassivationCapableBean(String id) {
        return getServices().get(ContextualStore.class).<Bean<Object>, Object>getContextual(id);
    }

    @Override
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
        final StereotypeModel<? extends Annotation> model = getServices().get(MetaAnnotationStore.class).getStereotype(stereotype);
        if (model.isValid()) {
            return model.getMetaAnnotations();
        } else {
            throw new IllegalArgumentException(NOT_STEREOTYPE, stereotype);
        }
    }

    @Override
    public boolean isQualifier(Class<? extends Annotation> annotationType) {
        return getServices().get(MetaAnnotationStore.class).getBindingTypeModel(annotationType).isValid();
    }

    @Override
    public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
        return getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(annotationType).isValid();
    }

    @Override
    public boolean isNormalScope(Class<? extends Annotation> annotationType) {
        ScopeModel<?> scope = getServices().get(MetaAnnotationStore.class).getScopeModel(annotationType);
        return scope.isValid() && scope.isNormal();
    }

    @Override
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
        ScopeModel<?> scope = getServices().get(MetaAnnotationStore.class).getScopeModel(annotationType);
        return scope.isValid() && scope.isPassivating();
    }

    @Override
    public boolean isScope(Class<? extends Annotation> annotationType) {
        return getServices().get(MetaAnnotationStore.class).getScopeModel(annotationType).isValid();
    }

    @Override
    public boolean isStereotype(Class<? extends Annotation> annotationType) {
        return getServices().get(MetaAnnotationStore.class).getStereotype(annotationType).isValid();
    }

    @Override
    public ELResolver getELResolver() {
        return weldELResolver;
    }

    @Override
    public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
        return new WeldExpressionFactory(expressionFactory);
    }

    @Override
    public <T> WeldCreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        return new CreationalContextImpl<T>(contextual);
    }

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
        return getServices().get(ClassTransformer.class).getAnnotatedType(type);
    }

    public <T> EnhancedAnnotatedType<T> createEnhancedAnnotatedType(Class<T> type) {
        return getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(type);
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        if (beans == null || beans.isEmpty()) {
            return null;
        }
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
        this.accessibleLenientObserverNotifier.clear();
        this.observers.clear();
    }

    public ConcurrentMap<Class<?>, InterceptionModel<ClassMetadata<?>, ?>> getInterceptorModelRegistry() {
        return interceptorModelRegistry;
    }

    public MetadataCachingReader getInterceptorMetadataReader() {
        return interceptorMetadataReader;
    }

    public <X> InjectionTarget<X> fireProcessInjectionTarget(AnnotatedType<X> annotatedType) {
        return services.get(ContainerLifecycleEvents.class).fireProcessInjectionTarget(this, annotatedType, createInjectionTarget(annotatedType));
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

    private static class InstanceInjectionPoint implements InjectionPoint, Serializable {

        private static final InjectionPoint INSTANCE = new InstanceInjectionPoint();

        private transient Type type;
        // there are no qualifiers by default
        // ResolvableBuilder.create() takes care of adding @Default if there is no qualifier selected
        private transient Set<Annotation> qualifiers = Collections.emptySet();

        public Type getType() {
            if (type == null) {
                this.type = new TypeLiteral<Instance<Object>>() {
                }.getType();
            }
            return type;
        }

        public Set<Annotation> getQualifiers() {
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

    @Override
    public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type) {
        EnhancedAnnotatedType<T> clazz = services.get(ClassTransformer.class).getEnhancedAnnotatedType(type);
        if (services.get(EjbDescriptors.class).contains(type.getJavaClass())) {
            return BeanAttributesFactory.forSessionBean(clazz, services.get(EjbDescriptors.class).getUnique(clazz.getJavaClass()), this);
        }
        return BeanAttributesFactory.forBean(clazz, this);
    }

    @Override
    public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> member) {
        return internalCreateBeanAttributes(member);
    }

    public <X> BeanAttributes<?> internalCreateBeanAttributes(AnnotatedMember<X> member) {
        EnhancedAnnotatedMember<?, X, Member> weldMember = null;
        if (member instanceof AnnotatedField<?> || member instanceof AnnotatedMethod<?>) {
            weldMember = services.get(MemberTransformer.class).loadEnhancedMember(member);
        } else {
            throw new IllegalArgumentException(INCORRECT_PRODUCER_MEMBER, member);
        }
        return BeanAttributesFactory.forBean(weldMember, this);
    }

    @Override
    public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTarget<T> injectionTarget) {
        return SyntheticBeanFactory.create(attributes, beanClass, injectionTarget, this);
    }

    @Override
    public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<?> beanClass, Producer<T> producer) {
        return SyntheticBeanFactory.create(attributes, beanClass, producer, this);
    }

    @Override
    public FieldInjectionPointAttributes<?, ?> createInjectionPoint(AnnotatedField<?> field) {
        AnnotatedTypeValidator.validateAnnotatedMember(field);
        return validateInjectionPoint(createFieldInjectionPoint(field));
    }

    private <X> FieldInjectionPointAttributes<?, X> createFieldInjectionPoint(AnnotatedField<X> field) {
        EnhancedAnnotatedField<?, X> enhancedField = services.get(MemberTransformer.class).loadEnhancedMember(field);
        return InferingFieldInjectionPointAttributes.of(enhancedField, null, field.getDeclaringType().getJavaClass(), this);
    }

    @Override
    public ParameterInjectionPointAttributes<?, ?> createInjectionPoint(AnnotatedParameter<?> parameter) {
        AnnotatedTypeValidator.validateAnnotatedParameter(parameter);
        EnhancedAnnotatedParameter<?, ?> enhancedParameter = services.get(MemberTransformer.class).load(parameter);
        return validateInjectionPoint(InferingParameterInjectionPointAttributes.of(enhancedParameter, null, parameter.getDeclaringCallable().getDeclaringType().getJavaClass(), this));
    }

    private <T extends InjectionPoint> T validateInjectionPoint(T injectionPoint) {
        try {
            services.get(Validator.class).validateInjectionPointForDefinitionErrors(injectionPoint, null, this);
        } catch (DefinitionException e) {
            throw new IllegalArgumentException(e);
        }
        return injectionPoint;
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> extensionClass) {
        Bean<?> bean = null;
        // resolve based on the bean class
        for (Bean<?> b : getBeans(extensionClass)) {
            if (b.getBeanClass().equals(extensionClass)) {
                bean = b;
            }
        }
        if (bean == null) {
            throw new IllegalArgumentException(NO_INSTANCE_OF_EXTENSION, extensionClass);
        }
        // We intentionally do not return a contextual instance, since it is not available at bootstrap.
        return extensionClass.cast(bean.create(null));
    }

    private boolean isRegisterableInjectionPoint(InjectionPoint ip) {
        // a delegate injection point is never registered (see CDI-78 for details)
        return ip != null && !ip.getType().equals(InjectionPoint.class) && !ip.isDelegate();
    }

    public ContainerLifecycleEvents getContainerLifecycleEvents() {
        return containerLifecycleEvents;
    }

    @Override
    public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2) {
        return Bindings.areQualifiersEquivalent(qualifier1, qualifier2, services.get(MetaAnnotationStore.class));
    }

    @Override
    public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2) {
        return Bindings.areInterceptorBindingsEquivalent(interceptorBinding1, interceptorBinding2, services.get(MetaAnnotationStore.class));
    }

    @Override
    public int getQualifierHashCode(Annotation qualifier) {
        return Bindings.getQualifierHashCode(qualifier, services.get(MetaAnnotationStore.class));
    }

    @Override
    public int getInterceptorBindingHashCode(Annotation interceptorBinding) {
        return Bindings.getInterceptorBindingHashCode(interceptorBinding, services.get(MetaAnnotationStore.class));
    }

    public <T> Collection<SlimAnnotatedType<T>> getSlimAnnotatedTypes(Class<T> type) {
        Preconditions.checkArgumentNotNull(type, "type");
        SlimAnnotatedTypeStore store = getServices().get(SlimAnnotatedTypeStore.class);
        return store.get(type);
    }

    @Override
    public <T> AnnotatedType<T> getAnnotatedType(Class<T> type, String id) {
        Preconditions.checkArgumentNotNull(type, "type");
        if (id == null) {
            id = type.getName();
        }
        for (SlimAnnotatedType<T> annotatedType : getSlimAnnotatedTypes(type)) {
            if (id.equals(annotatedType.getID())) {
                return annotatedType;
            }
        }
        return null;
    }

    @Override
    public <T> Iterable<AnnotatedType<T>> getAnnotatedTypes(Class<T> type) {
        return cast(getSlimAnnotatedTypes(type));
    }
}
