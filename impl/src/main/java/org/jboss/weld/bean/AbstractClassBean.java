/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;
import static org.jboss.weld.util.collections.WeldCollections.immutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedConstructorImpl;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.InterceptedSubclassFactory;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An abstract bean representation common for class-based beans
 *
 * @param <T> the type of class for the bean
 * @author Pete Muir
 * @author David Allen
 * @author Jozef Hartinger
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>> {

    // The item representation
    protected final AnnotatedType<T> annotatedType;
    protected volatile EnhancedAnnotatedType<T> enhancedAnnotatedItem;

    // The injectable fields of each type in the type hierarchy, with the actual
    // type at the bottom
    private List<Set<FieldInjectionPoint<?, ?>>> injectableFields;

    // The initializer methods of each type in the type hierarchy, with the
    // actual type at the bottom
    private List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;

    // Decorators
    private List<Decorator<?>> decorators;

    // Bean callback methods
    private List<AnnotatedMethod<? super T>> postConstructMethods;
    private List<AnnotatedMethod<? super T>> preDestroyMethods;

    // Injection target for the bean
    private InjectionTarget<T> injectionTarget;

    private final ConstructorInjectionPoint<T> constructor;

    protected EnhancedAnnotatedConstructor<T> constructorForEnhancedSubclass;

    private boolean passivationCapableBean;
    private boolean passivationCapableDependency;

    protected ProxyFactory<T> decoratorProxyFactory;

    private boolean hasInterceptors;
    private boolean subclassed;

    /**
     * Constructor
     *
     * @param type        The type
     * @param beanManager The Bean manager
     */
    protected AbstractClassBean(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, String idSuffix, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(attributes, idSuffix, beanManager, services);
        this.enhancedAnnotatedItem = type;
        this.annotatedType = type.slim();
        initType();
        this.constructor = initConstructor(beanManager);
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
        checkBeanImplementation();
        initPassivationCapable();
    }

    private void initPassivationCapable() {
        this.passivationCapableBean = getEnhancedAnnotated().isSerializable();
        if (getBeanManager().getServices().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal()) {
            this.passivationCapableDependency = true;
        } else if (getScope().equals(Dependent.class) && passivationCapableBean) {
            this.passivationCapableDependency = true;
        } else {
            this.passivationCapableDependency = false;
        }
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        initInterceptorsIfNeeded();
        initDecorators();
        if (this.passivationCapableBean && this.hasDecorators()) {
            for (Decorator<?> decorator : this.getDecorators()) {
                if (!(PassivationCapable.class.isAssignableFrom(decorator.getClass())) || !((WeldDecorator<?>) decorator).getEnhancedAnnotated().isSerializable()) {
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
        super.initializeAfterBeanDiscovery();
        subclassed = !Reflections.isFinal(getType()) && (hasDecorators() || hasInterceptors());
        if (isSubclassed()) {
            initEnhancedSubclass();
        }
        if (hasDecorators()) {
            decoratorProxyFactory = new ProxyFactory<T>(getType(), getTypes(), this);
            decoratorProxyFactory.getProxyClass(); //eagerly generate the proxy class
        }
    }

    protected void initInterceptorsIfNeeded() {
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(getType())) {
            new InterceptionModelInitializer<T>(beanManager, enhancedAnnotatedItem, this).init();
        }
        hasInterceptors = this.isInterceptionCandidate() && (beanManager.getInterceptorModelRegistry().containsKey(getType()));
    }

    public void initDecorators() {
        this.decorators = immutableList(getBeanManager().resolveDecorators(getTypes(), getQualifiers()));
    }

    public boolean hasDecorators() {
        return this.decorators != null && this.decorators.size() > 0;
    }

    protected T applyDecorators(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint) {
        assert hasDecorators() : "Bean does not have decorators";
        TargetBeanInstance beanInstance = new TargetBeanInstance(this, instance);
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanInstance, this, decoratorProxyFactory.getProxyClass(), beanManager, getContextualStore(), decorators);
        DecorationHelper.push(decorationHelper);
        final T outerDelegate;
        try {
            outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
        } finally {
            DecorationHelper.pop();
        }
        if (outerDelegate == null) {
            throw new WeldException(PROXY_INSTANTIATION_FAILED, this);
        }
        CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) ((ProxyObject) instance).getHandler();
        wrapperMethodHandler.setOuterDecorator(outerDelegate);
        return instance;
    }

    public List<Decorator<?>> getDecorators() {
        return decorators;
    }

    /**
     * Initializes the bean type
     */
    protected void initType() {
        this.type = getEnhancedAnnotated().getJavaClass();
    }

    /**
     * Initializes the injection points
     */
    protected void initInjectableFields(BeanManagerImpl manager) {
        injectableFields = InjectionPointFactory.instance().getFieldInjectionPoints(this, getEnhancedAnnotated(),  manager);
        addInjectionPoints(InjectionPoints.flattenInjectionPoints(injectableFields));
    }

    /**
     * Initializes the initializer methods
     */
    protected void initInitializerMethods(BeanManagerImpl manager) {
        initializerMethods = Beans.getInitializerMethods(this, getEnhancedAnnotated(), manager);
        addInjectionPoints(InjectionPoints.flattenParameterInjectionPoints(initializerMethods));
    }

    /**
     * Validates the bean implementation
     */
    protected void checkBeanImplementation() {
    }

    @Override
    protected void preSpecialize() {
        super.preSpecialize();
        if (getEnhancedAnnotated().getEnhancedSuperclass() == null || getEnhancedAnnotated().getEnhancedSuperclass().getJavaClass().equals(Object.class)) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        }
    }

    @Override
    public AnnotatedType<T> getAnnotated() {
        return annotatedType;
    }

    /**
     * Gets the annotated item
     *
     * @return The annotated item
     */
    @Override
    public EnhancedAnnotatedType<T> getEnhancedAnnotated() {
        return Beans.checkEnhancedAnnotatedAvailable(enhancedAnnotatedItem);
    }

    @Override
    public void cleanupAfterBoot() {
        super.cleanupAfterBoot();
        this.enhancedAnnotatedItem = null;
    }

    /**
     * Gets the annotated methods
     *
     * @return The set of annotated methods
     */
    public List<? extends Set<? extends MethodInjectionPoint<?, ?>>> getInitializerMethods() {
        return initializerMethods;
    }

    /**
     * @return the injectableFields
     */
    public List<? extends Set<FieldInjectionPoint<?, ?>>> getInjectableFields() {
        return injectableFields;
    }

    /**
     * Initializes the post-construct method
     */
    protected void initPostConstruct() {
        this.postConstructMethods = Beans.getPostConstructMethods(getEnhancedAnnotated());
    }

    /**
     * Initializes the pre-destroy method
     */
    protected void initPreDestroy() {
        this.preDestroyMethods = Beans.getPreDestroyMethods(getEnhancedAnnotated());
    }

    /**
     * Returns the post-construct method
     *
     * @return The post-construct method
     */
    public List<AnnotatedMethod<? super T>> getPostConstruct() {
        return postConstructMethods;
    }

    /**
     * Returns the pre-destroy method
     *
     * @return The pre-destroy method
     */
    public List<AnnotatedMethod<? super T>> getPreDestroy() {
        return preDestroyMethods;
    }

    protected abstract boolean isInterceptionCandidate();

    public void setInjectionTarget(InjectionTarget<T> injectionTarget) {
        this.injectionTarget = injectionTarget;
    }

    public InjectionTarget<T> getInjectionTarget() {
        return injectionTarget;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return getInjectionTarget().getInjectionPoints();
    }

    protected void defaultPreDestroy(T instance) {
        for (AnnotatedMethod<? super T> method : getPreDestroy()) {
            if (method != null) {
                try {
                    // note: RI supports injection into @PreDestroy
                    RuntimeAnnotatedMembers.invokeMethod(method, instance);
                } catch (Exception e) {
                    throw new WeldException(INVOCATION_ERROR, e, method, instance);
                }
            }
        }
    }

    protected void defaultPostConstruct(T instance) {
        for (AnnotatedMethod<? super T> method : getPostConstruct()) {
            if (method != null) {
                try {
                    // note: RI supports injection into @PreDestroy
                    RuntimeAnnotatedMembers.invokeMethod(method, instance);
                } catch (Exception e) {
                    throw new WeldException(INVOCATION_ERROR, e, method, instance);
                }
            }
        }
    }

    public boolean hasInterceptors() {
        return hasInterceptors;
    }

    protected void checkConstructor(EnhancedAnnotatedConstructor<T> enhancedAnnotated) {
        if (!enhancedAnnotated.getEnhancedParameters(Disposes.class).isEmpty()) {
            throw new DefinitionException(PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR, "@Disposes", constructor);
        }
        if (!enhancedAnnotated.getEnhancedParameters(Observes.class).isEmpty()) {
            throw new DefinitionException(PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR, "@Observes", constructor);
        }
    }

    /**
     * Initializes the constructor
     */
    protected ConstructorInjectionPoint<T> initConstructor(BeanManagerImpl manager) {
        EnhancedAnnotatedConstructor<T> enhancedAnnotated = Beans.getBeanConstructor(getEnhancedAnnotated());
        checkConstructor(enhancedAnnotated);
        ConstructorInjectionPoint<T> injectionPoint = InjectionPointFactory.instance().createConstructorInjectionPoint(this, getBeanClass(), enhancedAnnotated, manager);
        addInjectionPoints(injectionPoint.getParameterInjectionPoints());
        return injectionPoint;
    }

    /**
     * Returns the constructor
     *
     * @return The constructor
     */
    public ConstructorInjectionPoint<T> getConstructor() {
        return constructor;
    }

    protected boolean isSubclassed() {
        return subclassed;
    }

    protected void initEnhancedSubclass() {
        final ClassTransformer transformer = beanManager.getServices().get(ClassTransformer.class);
        EnhancedAnnotatedType<T> enhancedSubclass = transformer.getEnhancedAnnotatedType(createEnhancedSubclass());
        constructorForEnhancedSubclass = EnhancedAnnotatedConstructorImpl.of(
                enhancedSubclass.getDeclaredEnhancedConstructor(constructor.getSignature()),
                enhancedSubclass,
                transformer);
    }

    protected Class<T> createEnhancedSubclass() {
        Set<MethodSignature> enhancedMethodSignatures = new HashSet<MethodSignature>();
        for (AnnotatedMethod<?> method : Beans.getInterceptableMethods(this.getAnnotated())) {
            enhancedMethodSignatures.add(new MethodSignatureImpl(method));
        }
        return new InterceptedSubclassFactory<T>(getType(), getTypes(), this, enhancedMethodSignatures).getProxyClass();
    }

    @Override
    public boolean isPassivationCapableBean() {
        return passivationCapableBean;
    }

    @Override
    public boolean isPassivationCapableDependency() {
        return passivationCapableDependency;
    }

    private ContextualStore getContextualStore() {
        return getServices().get(ContextualStore.class);
    }
}
