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

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.ONLY_ONE_SCOPE_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.USING_DEFAULT_SCOPE;
import static org.jboss.weld.logging.messages.BeanMessage.USING_SCOPE;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Scope;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.InterceptedSubclassFactory;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.MethodSignatureImpl;
import org.jboss.weld.introspector.jlr.WeldConstructorImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import javassist.util.proxy.ProxyObject;

/**
 * An abstract bean representation common for class-based beans
 *
 * @param <T> the type of class for the bean
 * @author Pete Muir
 * @author David Allen
 * @author Marko Luksa
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>> {

    // Logger
    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    // The item representation
    protected WeldClass<T> annotatedItem;

    // The injectable fields of each type in the type hierarchy, with the actual
    // type at the bottom
    private List<Set<FieldInjectionPoint<?, ?>>> injectableFields;

    // The initializer methods of each type in the type hierarchy, with the
    // actual type at the bottom
    private List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;

    // Decorators
    private List<Decorator<?>> decorators;

    // Bean callback methods
    private List<WeldMethod<?, ? super T>> postConstructMethods;
    private List<WeldMethod<?, ? super T>> preDestroyMethods;

    // Injection target for the bean
    private InjectionTarget<T> injectionTarget;

    private ConstructorInjectionPoint<T> constructor;

    protected WeldClass<T> enhancedSubclass;

    protected WeldConstructor<T> constructorForEnhancedSubclass;

    protected ProxyFactory<T> decoratorProxyFactory;

    private boolean subclassed;

    /**
     * Constructor
     *
     * @param type        The type
     * @param beanManager The Bean manager
     */
    protected AbstractClassBean(WeldClass<T> type, String idSuffix, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(idSuffix, beanManager, services);
        this.annotatedItem = type;
        initStereotypes();
        initAlternative();
        initInitializerMethods();
        initInjectableFields();
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        super.initialize(environment);
        checkBeanImplementation();
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        initInterceptorsIfNeeded();
        initDecorators();
        initAfterInterceptorsAndDecoratorsInitialized();
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

    protected void initAfterInterceptorsAndDecoratorsInitialized() {
    }

    protected void initInterceptorsIfNeeded() {
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(getType())) {
            new InterceptionModelInitializer<T>(beanManager, getWeldAnnotated(), this, getType()).init();
        }
    }

    public void initDecorators() {
        this.decorators = getBeanManager().resolveDecorators(getTypes(), getQualifiers());
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
        return Collections.unmodifiableList(decorators);
    }

    /**
     * Initializes the bean type
     */
    protected void initType() {
        this.type = getWeldAnnotated().getJavaClass();
    }

    /**
     * Initializes the injection points
     */
    protected void initInjectableFields() {
        injectableFields = Beans.getFieldInjectionPoints(this, annotatedItem, beanManager);
        addInjectionPoints(Beans.mergeFieldInjectionPoints(injectableFields));
    }

    /**
     * Initializes the initializer methods
     */
    protected void initInitializerMethods() {
        initializerMethods = Beans.getInitializerMethods(this, getWeldAnnotated(), beanManager);
        addInjectionPoints(Beans.getParameterInjectionPoints(this, initializerMethods, beanManager));
    }

    @Override
    protected void initScope() {
        for (WeldClass<?> clazz = getWeldAnnotated(); clazz != null; clazz = clazz.getWeldSuperclass()) {
            Set<Annotation> scopes = new HashSet<Annotation>();
            scopes.addAll(clazz.getDeclaredMetaAnnotations(Scope.class));
            scopes.addAll(clazz.getDeclaredMetaAnnotations(NormalScope.class));
            if (scopes.size() == 1) {
                if (getWeldAnnotated().isAnnotationPresent(scopes.iterator().next().annotationType())) {
                    this.scope = scopes.iterator().next().annotationType();
                    log.trace(USING_SCOPE, scope, this);
                }
                break;
            } else if (scopes.size() > 1) {
                throw new DefinitionException(ONLY_ONE_SCOPE_ALLOWED, getWeldAnnotated());
            }
        }

        if (this.scope == null) {
            initScopeFromStereotype();
        }

        if (this.scope == null) {
            this.scope = Dependent.class;
            log.trace(USING_DEFAULT_SCOPE, this);
        }
    }

    /**
     * Validates the bean implementation
     */
    protected void checkBeanImplementation() {
    }

    @Override
    protected void preSpecialize(BeanDeployerEnvironment environment) {
        super.preSpecialize(environment);
        if (getWeldAnnotated().getWeldSuperclass() == null || getWeldAnnotated().getWeldSuperclass().getJavaClass().equals(Object.class)) {
            throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
        }
    }

    /**
     * Gets the annotated item
     *
     * @return The annotated item
     */
    @Override
    public WeldClass<T> getWeldAnnotated() {
        return annotatedItem;
    }

    /**
     * Gets the default name
     *
     * @return The default name
     */
    @Override
    protected String getDefaultName() {
        return Introspector.decapitalize(getWeldAnnotated().getSimpleName());
    }

    /**
     * Gets the annotated methods
     *
     * @return The set of annotated methods
     */
    public List<? extends Set<? extends MethodInjectionPoint<?, ?>>> getInitializerMethods() {
        return Collections.unmodifiableList(initializerMethods);
    }

    /**
     * @return the injectableFields
     */
    public List<? extends Set<FieldInjectionPoint<?, ?>>> getInjectableFields() {
        return Collections.unmodifiableList(injectableFields);
    }

    /**
     * Initializes the post-construct method
     */
    protected void initPostConstruct() {
        this.postConstructMethods = Beans.getPostConstructMethods(getWeldAnnotated());
    }

    /**
     * Initializes the pre-destroy method
     */
    protected void initPreDestroy() {
        this.preDestroyMethods = Beans.getPreDestroyMethods(getWeldAnnotated());
    }

    /**
     * Returns the post-construct method
     *
     * @return The post-construct method
     */
    public List<WeldMethod<?, ? super T>> getPostConstruct() {
        return postConstructMethods;
    }

    /**
     * Returns the pre-destroy method
     *
     * @return The pre-destroy method
     */
    public List<WeldMethod<?, ? super T>> getPreDestroy() {
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
        for (WeldMethod<?, ? super T> method : getPreDestroy()) {
            if (method != null) {
                try {
                    // note: RI supports injection into @PreDestroy
                    method.invoke(instance);
                } catch (Exception e) {
                    throw new WeldException(INVOCATION_ERROR, e, method, instance);
                }
            }
        }
    }

    protected void defaultPostConstruct(T instance) {
        for (WeldMethod<?, ? super T> method : getPostConstruct()) {
            if (method != null) {
                try {
                    // note: RI supports injection into @PreDestroy
                    method.invoke(instance);
                } catch (Exception e) {
                    throw new WeldException(INVOCATION_ERROR, e, method, instance);
                }
            }
        }
    }

    public boolean hasInterceptors() {
        if (isInterceptionCandidate()) {
            return beanManager.getInterceptorModelRegistry().get(getType()) != null;
        } else {
            return false;
        }
    }

    protected void checkConstructor() {
        if (!constructor.getWeldParameters(Disposes.class).isEmpty()) {
            throw new DefinitionException(PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR, "@Disposes", constructor);
        }
        if (!constructor.getWeldParameters(Observes.class).isEmpty()) {
            throw new DefinitionException(PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR, "@Observes", constructor);
        }
    }

    /**
     * Initializes the constructor
     */
    protected void initConstructor() {
        this.constructor = Beans.getBeanConstructor(this, getWeldAnnotated(), beanManager);
        addInjectionPoints(Beans.getParameterInjectionPoints(this, constructor, beanManager));
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
        enhancedSubclass = transformer.loadClass(createEnhancedSubclass());
        constructorForEnhancedSubclass = WeldConstructorImpl.of(
                enhancedSubclass.getDeclaredWeldConstructor(getConstructor().getSignature()),
                enhancedSubclass,
                transformer);
    }

    protected Class<T> createEnhancedSubclass() {
        Set<MethodSignature> enhancedMethodSignatures = new HashSet<MethodSignature>();
        for (WeldMethod<?, ?> method : Beans.getInterceptableMethods(this.getWeldAnnotated())) {
            enhancedMethodSignatures.add(new MethodSignatureImpl(method));
        }
        return new InterceptedSubclassFactory<T>(getType(), getTypes(), this, enhancedMethodSignatures).getProxyClass();
    }

    private ContextualStore getContextualStore() {
        return getServices().get(ContextualStore.class);
    }

}
