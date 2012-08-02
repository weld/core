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
package org.jboss.weld.bean;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jboss.weld.Container;
import org.jboss.weld.bean.interceptor.WeldInterceptorClassMetadata;
import org.jboss.weld.bean.interceptor.WeldInterceptorInstantiator;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.ProxyClassConstructorInjectionPointWrapper;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.interceptor.proxy.DefaultInvocationContextFactory;
import org.jboss.weld.interceptor.proxy.InterceptorProxyCreatorImpl;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLogger.Level;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_INJECTION_POINT_NOT_FOUND;
import static org.jboss.weld.logging.messages.BeanMessage.ERROR_DESTROYING;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.NON_CONTAINER_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL;
import static org.jboss.weld.logging.messages.BeanMessage.PUBLIC_FIELD_ON_NORMAL_SCOPED_BEAN_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;
import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * Represents a simple bean
 *
 * @param <T> The type (class) of the bean
 * @author Pete Muir
 * @author Marius Bogoevici
 * @author Ales Justin
 */
public class ManagedBean<T> extends AbstractClassBean<T> {

    private abstract static class FixInjectionPoint<T> {

        private final AbstractClassBean<T> bean;

        private InjectionPoint originalInjectionPoint;

        private FixInjectionPoint(AbstractClassBean<T> bean) {
            this.bean = bean;
        }

        protected abstract T work();

        private void setup() {
            if (bean.hasDecorators()) {
                Decorator<?> decorator = bean.getDecorators().get(bean.getDecorators().size() - 1);
                InjectionPoint outerDelegateInjectionPoint = Beans.getDelegateInjectionPoint(decorator);
                if (outerDelegateInjectionPoint == null) {
                    throw new IllegalStateException(DELEGATE_INJECTION_POINT_NOT_FOUND, decorator);
                }
                CurrentInjectionPoint currentInjectionPoint = Container.instance(bean.beanManager.getContextId()).services().get(CurrentInjectionPoint.class);
                if (currentInjectionPoint.peek() != null) {
                    this.originalInjectionPoint = currentInjectionPoint.pop();
                    currentInjectionPoint.push(outerDelegateInjectionPoint);
                } else {
                    currentInjectionPoint.push(outerDelegateInjectionPoint);
                }
            }
        }

        public InjectionPoint getOriginalInjectionPoint() {
            return originalInjectionPoint;
        }

        private void cleanup() {
            if (bean.hasDecorators()) {
                String contextId = bean.beanManager.getContextId();
                final CurrentInjectionPoint currentInjectionPoint = Container.instance(contextId).services().get(CurrentInjectionPoint.class);
                currentInjectionPoint.pop();
                currentInjectionPoint.push(originalInjectionPoint);
            }
        }

        public T run() {
            try {
                setup();
                return work();
            } finally {
                cleanup();
            }

        }

    }

    private static class ManagedBeanInjectionTarget<T> implements InjectionTarget<T> {

        private final ManagedBean<T> bean;

        private ManagedBeanInjectionTarget(ManagedBean<T> bean) {
            this.bean = bean;
        }

        protected ManagedBean<T> getBean() {
            return bean;
        }

        public void inject(final T instance, final CreationalContext<T> ctx) {
            new FixInjectionPoint<T>(bean) {

                @Override
                protected T work() {
                    new InjectionContextImpl<T>(bean.getBeanManager(), ManagedBeanInjectionTarget.this, getBean().getWeldAnnotated(), instance) {

                        public void proceed() {
                            Beans.injectEEFields(instance, bean.getBeanManager(), bean.ejbInjectionPoints, bean.persistenceContextInjectionPoints, bean.persistenceUnitInjectionPoints, bean.resourceInjectionPoints);
                            Beans.injectFieldsAndInitializers(instance, ctx, bean.getBeanManager(), bean.getInjectableFields(), bean.getInitializerMethods());
                        }

                    }.run();
                    return null;
                }
            }.run();
        }

        public void postConstruct(T instance) {
            if (bean.hasInterceptors()) {
                InterceptionUtils.executePostConstruct(instance);
            } else {
                bean.defaultPostConstruct(instance);
            }
        }

        public void preDestroy(T instance) {
            if (bean.hasInterceptors()) {
                InterceptionUtils.executePredestroy(instance);
            } else {
                bean.defaultPreDestroy(instance);
            }
        }

        public void dispose(T instance) {
            // No-op
        }

        public Set<InjectionPoint> getInjectionPoints() {
            return cast(bean.getWeldInjectionPoints());
        }

        public T produce(final CreationalContext<T> ctx) {
            T instance;
            if (!bean.hasDecorators()) {
                // This should be safe, but needs verification PLM
                // Without this, the chaining of decorators will fail as the
                // incomplete instance will be resolved
                instance = bean.createInstance(ctx);
                // Do not keep dependent instances as incomplete
                if (bean.isDependent() == false) {
                    ctx.push(instance);
                }
            } else {
                instance = new FixInjectionPoint<T>(bean) {
                    @Override
                    protected T work() {
                        // for decorated beans, creation should use the fixed injection point
                        // thus ensuring that the innermost decorator is provided as InjectionPoint
                        T undecoratedInstance = bean.createInstance(ctx);
                        return bean.applyDecorators(undecoratedInstance, ctx, getOriginalInjectionPoint());
                    }

                }.run();
            }
            if (bean.hasInterceptors()) {
                return bean.applyInterceptors(instance, ctx);
            } else {
                return instance;
            }
        }


    }

    // Logger
    private static final LocLogger log = loggerFactory().getLogger(BEAN);
    private static final XLogger xLog = loggerFactory().getXLogger(BEAN);

    // The Java EE style injection points
    private Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints;
    private Set<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints;
    private Set<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
    private Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints;

    private ManagedBean<?> specializedBean;

    private boolean passivationCapableBean;
    private boolean passivationCapableDependency;
    private final boolean proxiable;

    /**
     * Creates a simple, annotation defined Web Bean
     *
     * @param <T>         The type
     * @param clazz       The class
     * @param beanManager the current manager
     * @return A Web Bean
     */
    public static <T> ManagedBean<T> of(WeldClass<T> clazz, BeanManagerImpl beanManager, ServiceRegistry services) {
        if (clazz.isDiscovered()) {
            return new ManagedBean<T>(clazz, createSimpleId(ManagedBean.class.getSimpleName(), clazz), beanManager, services);
        } else {
            return new ManagedBean<T>(clazz, createId(ManagedBean.class.getSimpleName(), clazz), beanManager, services);
        }
    }

    protected static String createSimpleId(String beanType, WeldClass<?> clazz) {
        return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(clazz.getBaseType()).toString();
    }

    /**
     * create a more complete id for types that have been added through the SPI
     * to prevent duplicate id's
     */
    protected static String createId(String beanType, WeldClass<?> clazz) {
        return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(AnnotatedTypes.createTypeId(clazz)).toString();
    }

    /**
     * Constructor
     *
     * @param type        The type of the bean
     * @param beanManager The Bean manager
     */
    protected ManagedBean(WeldClass<T> type, String idSuffix, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(type, idSuffix, beanManager, services);
        initType();
        initTypes();
        initQualifiers();
        initConstructor();
        this.proxiable = Proxies.isTypesProxyable(getTypes(), beanManager.getContextId());
    }

    /**
     * Creates an instance of the bean
     *
     * @return The instance
     */
    public T create(CreationalContext<T> creationalContext) {
        T instance = getInjectionTarget().produce(creationalContext);
        getInjectionTarget().inject(instance, creationalContext);
        getInjectionTarget().postConstruct(instance);
        return instance;
    }

    /**
     * Destroys an instance of the bean
     *
     * @param instance The instance
     */
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        try {
            getInjectionTarget().preDestroy(instance);
            // WELD-1010 hack?
            if (creationalContext instanceof CreationalContextImpl) {
                ((CreationalContextImpl<T>) creationalContext).release(this, instance);
            } else {
                creationalContext.release();
            }
        } catch (Exception e) {
            log.error(ERROR_DESTROYING, this, instance);
            xLog.throwing(Level.DEBUG, e);
        }
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        if (!isInitialized()) {
            checkConstructor();
            super.initialize(environment);
            initPostConstruct();
            initPreDestroy();
            initEEInjectionPoints();
            initPassivationCapable();
            setInjectionTarget(new ManagedBeanInjectionTarget<T>(this));
        }
    }

    protected T createInstance(CreationalContext<T> ctx) {
        if (!isSubclassed()) {
            return getConstructor().newInstance(beanManager, ctx);
        } else {
            ProxyClassConstructorInjectionPointWrapper<T> constructorInjectionPointWrapper = new ProxyClassConstructorInjectionPointWrapper<T>(beanManager.getContextId(), this, constructorForEnhancedSubclass, getConstructor());
            return constructorInjectionPointWrapper.newInstance(beanManager, ctx);
        }
    }

    @Override
    protected void initAfterInterceptorsAndDecoratorsInitialized() {
        super.initAfterInterceptorsAndDecoratorsInitialized();

        if (this.passivationCapableBean && this.hasDecorators()) {
            for (Decorator<?> decorator : this.getDecorators()) {
                if (!(PassivationCapable.class.isAssignableFrom(decorator.getClass())) || !((WeldDecorator<?>) decorator).getWeldAnnotated().isSerializable()) {
                    this.passivationCapableBean = false;
                    break;
                }
            }
        }
        if (this.passivationCapableBean && hasInterceptors()) {
            for (InterceptorMetadata<?> interceptorMetadata : getBeanManager().getInterceptorModelRegistry().get(getType()).getAllInterceptors()) {
                if (!Reflections.isSerializable(interceptorMetadata.getInterceptorClass().getJavaClass())) {
                    this.passivationCapableBean = false;
                    break;
                }
            }
        }
    }

    private void initPassivationCapable() {
        this.passivationCapableBean = getWeldAnnotated().isSerializable();
        if (Container.instance(beanManager.getContextId()).services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal()) {
            this.passivationCapableDependency = true;
        } else if (getScope().equals(Dependent.class) && passivationCapableBean) {
            this.passivationCapableDependency = true;
        } else {
            this.passivationCapableDependency = false;
        }
    }

    @Override
    public boolean isPassivationCapableBean() {
        return passivationCapableBean;
    }

    @Override
    public boolean isPassivationCapableDependency() {
        return passivationCapableDependency;
    }

    private void initEEInjectionPoints() {
        this.ejbInjectionPoints = Beans.getEjbInjectionPoints(beanManager.getContextId(), this, getWeldAnnotated(), getBeanManager());
        this.persistenceContextInjectionPoints = Beans.getPersistenceContextInjectionPoints(beanManager.getContextId(), this, getWeldAnnotated(), getBeanManager());
        this.persistenceUnitInjectionPoints = Beans.getPersistenceUnitInjectionPoints(beanManager.getContextId(), this, getWeldAnnotated(), getBeanManager());
        this.resourceInjectionPoints = Beans.getResourceInjectionPoints(beanManager.getContextId(), this, getWeldAnnotated(), beanManager);
    }

    /**
     * Validates the type
     */
    @Override
    protected void checkType() {
        if (getWeldAnnotated().isAnonymousClass() || (getWeldAnnotated().isMemberClass() && !getWeldAnnotated().isStatic())) {
            throw new DefinitionException(SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED, type);
        }
        if (!isDependent() && getWeldAnnotated().isParameterizedType()) {
            throw new DefinitionException(BEAN_MUST_BE_DEPENDENT, type);
        }
        boolean passivating = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(scope).isPassivating();
        if (passivating && !isPassivationCapableBean()) {
            throw new DefinitionException(PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL, this);
        }
        if (hasDecorators()) {
            if (getWeldAnnotated().isFinal()) {
                throw new DefinitionException(FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED, this);
            }
            for (Decorator<?> decorator : getDecorators()) {
                WeldClass<?> decoratorClass;
                if (decorator instanceof DecoratorImpl<?>) {
                    DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
                    decoratorClass = decoratorBean.getWeldAnnotated();
                } else if (decorator instanceof CustomDecoratorWrapper<?>) {
                    decoratorClass = ((CustomDecoratorWrapper<?>) decorator).getWeldAnnotated();
                } else {
                    throw new IllegalStateException(NON_CONTAINER_DECORATOR, decorator);
                }

                for (WeldMethod<?, ?> decoratorMethod : decoratorClass.getWeldMethods()) {
                    WeldMethod<?, ?> method = getWeldAnnotated().getWeldMethod(decoratorMethod.getSignature());
                    if (method != null && !method.isStatic() && !method.isPrivate() && method.isFinal()) {
                        throw new DefinitionException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, method, decoratorMethod);
                    }
                }
            }
        }
    }

    @Override
    protected void checkBeanImplementation() {
        super.checkBeanImplementation();
        if (isNormalScoped()) {
            for (WeldField<?, ?> field : getWeldAnnotated().getWeldFields()) {
                if (field.isPublic() && !field.isStatic()) {
                    throw new DefinitionException(PUBLIC_FIELD_ON_NORMAL_SCOPED_BEAN_NOT_ALLOWED, field);
                }
            }
        }
    }

    @Override
    protected void preSpecialize(BeanDeployerEnvironment environment) {
        super.preSpecialize(environment);
        BeansClosure closure = beanManager.getClosure();
        if (closure.isEJB(getWeldAnnotated().getWeldSuperclass())) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        }
    }

    @Override
    protected void specialize(BeanDeployerEnvironment environment) {
        BeansClosure closure = beanManager.getClosure();
        Bean<?> specializedBean = closure.getClassBean(getWeldAnnotated().getWeldSuperclass());
        if (specializedBean == null) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        }
        if (!(specializedBean instanceof ManagedBean<?>)) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        } else {
            this.specializedBean = (ManagedBean<?>) specializedBean;
        }
    }

    @Override
    public ManagedBean<?> getSpecializedBean() {
        return specializedBean;
    }

    @Override
    protected boolean isInterceptionCandidate() {
        return !Beans.isInterceptor(getWeldAnnotated()) && !Beans.isDecorator(getWeldAnnotated());
    }

    protected T applyInterceptors(T instance, final CreationalContext<T> creationalContext) {
        try {
            WeldInterceptorInstantiator<T> interceptorInstantiator = new WeldInterceptorInstantiator<T>(beanManager, creationalContext);
            InterceptorProxyCreatorImpl interceptorProxyCreator = new InterceptorProxyCreatorImpl(interceptorInstantiator, new DefaultInvocationContextFactory(), beanManager.getInterceptorModelRegistry().get(getType()));
            MethodHandler methodHandler = interceptorProxyCreator.createSubclassingMethodHandler(null, WeldInterceptorClassMetadata.of(getWeldAnnotated()));
            CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) ((ProxyObject) instance).getHandler();
            wrapperMethodHandler.setInterceptorMethodHandler(methodHandler);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return instance;
    }

    @Override
    public String toString() {
        return "Managed Bean [" + getBeanClass().toString() + "] with qualifiers [" + Formats.formatAnnotations(getQualifiers()) + "]";
    }

    @Override
    public boolean isProxyable() {
        return proxiable;
    }

    @Override
    public boolean hasDefaultProducer() {
        return getInjectionTarget() instanceof ManagedBean.ManagedBeanInjectionTarget;
    }

}
