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

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Scope;

import javassist.util.proxy.ProxyObject;
import org.jboss.weld.bean.interceptor.CustomInterceptorMetadata;
import org.jboss.weld.bean.interceptor.SerializableContextualInterceptorReference;
import org.jboss.weld.bean.interceptor.WeldInterceptorClassMetadata;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.InterceptedSubclassFactory;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.context.SerializableContextualImpl;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.interceptor.InterceptorBindingType;
import org.jboss.weld.interceptor.builder.InterceptionModelBuilder;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.MethodSignatureImpl;
import org.jboss.weld.introspector.jlr.WeldConstructorImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;
import org.slf4j.cal10n.LocLogger;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.ONLY_ONE_SCOPE_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.USING_DEFAULT_SCOPE;
import static org.jboss.weld.logging.messages.BeanMessage.USING_SCOPE;
import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * An abstract bean representation common for class-based beans
 *
 * @param <T> the type of class for the bean
 * @author Pete Muir
 * @author David Allen
 * @author Marko Luksa
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>> {

    private static final InterceptorMetadata<?>[] EMPTY_INTERCEPTOR_METADATA_ARRAY = new InterceptorMetadata[0];

    private static <T> InterceptorMetadata<T>[] emptyInterceptorMetadataArray() {
        return cast(EMPTY_INTERCEPTOR_METADATA_ARRAY);
    }

    /**
     * Extracts the complete set of interception bindings from a given set of
     * annotations.
     *
     * @param beanManager
     * @param annotations
     * @return
     */
    protected static Set<Annotation> flattenInterceptorBindings(BeanManagerImpl beanManager, Set<Annotation> annotations) {
        Set<Annotation> foundInterceptionBindingTypes = new HashSet<Annotation>();
        for (Annotation annotation : annotations) {
            if (beanManager.isInterceptorBinding(annotation.annotationType())) {
                foundInterceptionBindingTypes.add(annotation);
                foundInterceptionBindingTypes.addAll(beanManager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(annotation.annotationType()).getInheritedInterceptionBindingTypes());
            }
        }
        return foundInterceptionBindingTypes;
    }

    private InterceptorMetadata<SerializableContextual<?, ?>>[] toSerializableContextualArray(List<Interceptor<?>> interceptors) {
        List<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> serializableContextuals = new ArrayList<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();
        for (Interceptor<?> interceptor : interceptors) {

            SerializableContextualImpl<Interceptor<?>, ?> contextual = new SerializableContextualImpl(getBeanManager().getContextId(), interceptor, getServices().get(ContextualStore.class));
            serializableContextuals.add(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(new SerializableContextualInterceptorReference(contextual, beanManager.getInterceptorMetadataReader().getClassMetadata(interceptor.getBeanClass()))));
        }
        return serializableContextuals.toArray(AbstractClassBean.<SerializableContextual<?, ?>>emptyInterceptorMetadataArray());
    }

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

    // Interceptors
    private boolean hasSerializationOrInvocationInterceptorMethods;

    // Bean callback methods
    private List<WeldMethod<?, ? super T>> postConstructMethods;
    private List<WeldMethod<?, ? super T>> preDestroyMethods;

    // Injection target for the bean
    private InjectionTarget<T> injectionTarget;

    private ConstructorInjectionPoint<T> constructor;

    protected WeldClass<T> enhancedSubclass;

    protected WeldConstructor<T> constructorForEnhancedSubclass;

    /**
     *
     */
    protected ProxyFactory<T> decoratorProxyFactory;

    private boolean hasInterceptors;
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
        initTargetClassInterceptors();
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
            decoratorProxyFactory = new ProxyFactory<T>(getBeanManager().getContextId(), getType(), getTypes(), this);
            decoratorProxyFactory.getProxyClass(); //eagerly generate the proxy class
        }
    }

    protected void initAfterInterceptorsAndDecoratorsInitialized() {
    }

    protected void initInterceptorsIfNeeded() {
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(getType())) {
            new InterceptionModelInitializer().init();
        }
        hasInterceptors = this.isInterceptionCandidate() && (hasSerializationOrInvocationInterceptorMethods || beanManager.getInterceptorModelRegistry().get(getType()) != null);
    }

    public void initDecorators() {
        this.decorators = getBeanManager().resolveDecorators(getTypes(), getQualifiers());
    }

    public boolean hasDecorators() {
        return this.decorators != null && this.decorators.size() > 0;
    }

    protected T applyDecorators(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint) {
        T outerDelegate = getOuterDelegate(instance, creationalContext, originalInjectionPoint);
        registerOuterDecorator((ProxyObject) instance, outerDelegate);
        return instance;
    }

    protected T getOuterDelegate(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint) {
        assert hasDecorators() : "Bean does not have decorators";
        TargetBeanInstance beanInstance = new TargetBeanInstance(this, instance);
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanManager.getContextId(), beanInstance, this, decoratorProxyFactory.getProxyClass(), beanManager, getContextualStore(), decorators);
        DecorationHelper.push(decorationHelper);
        try {
            final T outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
            if (outerDelegate == null) {
                throw new WeldException(PROXY_INSTANTIATION_FAILED, this);
            }
            return outerDelegate;
        } finally {
            DecorationHelper.pop();
        }
    }

    private void registerOuterDecorator(ProxyObject instance, T outerDelegate) {
        CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) instance.getHandler();
        wrapperMethodHandler.setOuterDecorator(outerDelegate);
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
        injectableFields = Beans.getFieldInjectionPoints(beanManager.getContextId(), this, annotatedItem);
        addInjectionPoints(Beans.mergeFieldInjectionPoints(injectableFields));
    }

    /**
     * Initializes the initializer methods
     */
    protected void initInitializerMethods() {
        initializerMethods = Beans.getInitializerMethods(beanManager.getContextId(), this, getWeldAnnotated());
        addInjectionPoints(Beans.getParameterInjectionPoints(beanManager.getContextId(), this, initializerMethods));
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
        return hasInterceptors;
    }

    private void initTargetClassInterceptors() {
        if (!Beans.isInterceptor(getWeldAnnotated())) {
            InterceptorMetadata<T> interceptorClassMetadata = beanManager.getInterceptorMetadataReader().getTargetClassInterceptorMetadata(WeldInterceptorClassMetadata.of(getWeldAnnotated()));
            hasSerializationOrInvocationInterceptorMethods = interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.AROUND_INVOKE)
                    || interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.AROUND_TIMEOUT)
                    || interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_PASSIVATE)
                    || interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.POST_ACTIVATE);
        } else {
            // an interceptor does not have lifecycle methods of its own, but it intercepts the methods of the
            // target class
            hasSerializationOrInvocationInterceptorMethods = false;
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
        this.constructor = Beans.getBeanConstructor(beanManager.getContextId(), this, getWeldAnnotated());
        addInjectionPoints(Beans.getParameterInjectionPoints(beanManager.getContextId(), this, constructor));
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
        enhancedSubclass = beanManager.getServices().get(ClassTransformer.class).loadClass(createEnhancedSubclass());
        constructorForEnhancedSubclass = WeldConstructorImpl.of(getBeanManager().getContextId(),
                enhancedSubclass.getDeclaredWeldConstructor(getConstructor().getSignature()),
                enhancedSubclass,
                transformer);
    }

    protected Class<T> createEnhancedSubclass() {
        Set<MethodSignature> enhancedMethodSignatures = new HashSet<MethodSignature>();
        for (WeldMethod<?, ?> method : Beans.getInterceptableMethods(this.getWeldAnnotated())) {
            enhancedMethodSignatures.add(new MethodSignatureImpl(method));
        }
        return new InterceptedSubclassFactory<T>(getBeanManager().getContextId(), getType(), Collections.<Type>emptySet(), this, enhancedMethodSignatures).getProxyClass();
    }

    private ContextualStore getContextualStore() {
        return getServices().get(ContextualStore.class);
    }


    private class InterceptionModelInitializer {

        private Map<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> interceptorMetadatas = new HashMap<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();

        private List<WeldMethod<?,?>> businessMethods;
        private InterceptionModelBuilder<ClassMetadata<?>,?> builder;

        public void init() {
            businessMethods = Beans.getInterceptableMethods(getWeldAnnotated());
            builder = InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(getClassMetadata());

            initEjbInterceptors();
            initCdiInterceptors();

            InterceptionModel<ClassMetadata<?>, ?> interceptionModel = builder.build();
            if (interceptionModel.getAllInterceptors().size() > 0 || hasSerializationOrInvocationInterceptorMethods) {
                if (getWeldAnnotated().isFinal()) {
                    throw new DefinitionException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, AbstractClassBean.this);
                }
                beanManager.getInterceptorModelRegistry().put(getType(), interceptionModel);
            }
        }

        private ClassMetadata<T> getClassMetadata() {
            return beanManager.getInterceptorMetadataReader().getClassMetadata(getType());
        }

        private void initCdiInterceptors() {
            Set<InterceptorBindingType> classBindings = getClassInterceptorBindings();
            initCdiLifecycleInterceptors(classBindings);
            initCdiBusinessMethodInterceptors(classBindings);
        }

        private Set<InterceptorBindingType> getClassInterceptorBindings() {
            Set<InterceptorBindingType> classBindingAnnotations = new HashSet<InterceptorBindingType>();
            classBindingAnnotations.addAll(beanManager.extractAndFlattenInterceptorBindings(getWeldAnnotated().getAnnotations()));
            for (Class<? extends Annotation> annotation : getStereotypes()) {
                classBindingAnnotations.addAll(beanManager.extractAndFlattenInterceptorBindings(beanManager.getStereotypeDefinition(annotation)));
            }
            return classBindingAnnotations;
        }

        private void initCdiLifecycleInterceptors(Set<InterceptorBindingType> classBindings) {
            if (classBindings.size() == 0) {
                return;
            }
            if (Beans.findInterceptorBindingConflicts(classBindings)) {
                throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType());
            }

            initLifeCycleInterceptor(InterceptionType.POST_CONSTRUCT, classBindings);
            initLifeCycleInterceptor(InterceptionType.PRE_DESTROY, classBindings);
            initLifeCycleInterceptor(InterceptionType.PRE_PASSIVATE, classBindings);
            initLifeCycleInterceptor(InterceptionType.POST_ACTIVATE, classBindings);
        }

        private void initLifeCycleInterceptor(InterceptionType interceptionType, Set<InterceptorBindingType> interceptorBindingTypes) {
            List<Interceptor<?>> resolvedInterceptors = beanManager.resolveInterceptors(interceptionType, interceptorBindingTypes);
            builder.intercept(interceptionType).with(toSerializableContextualArray(resolvedInterceptors));
        }

        private void initCdiBusinessMethodInterceptors(Set<InterceptorBindingType> classBindings) {
            for (WeldMethod<?, ?> method : businessMethods) {
                initCdiBusinessMethodInterceptor(method, getMethodInterceptorBindings(classBindings, method));
            }
        }

        private Set<InterceptorBindingType> getMethodInterceptorBindings(Set<InterceptorBindingType> classBindingAnnotations, WeldMethod<?, ?> method) {
            Set<InterceptorBindingType> methodBindingAnnotations = new HashSet<InterceptorBindingType>();
            methodBindingAnnotations.addAll(classBindingAnnotations);
            methodBindingAnnotations.addAll(beanManager.extractAndFlattenInterceptorBindings(method.getAnnotations()));
            return methodBindingAnnotations;
        }

        private void initCdiBusinessMethodInterceptor(WeldMethod<?, ?> method, Set<InterceptorBindingType> methodInterceptorBindings) {
            if (methodInterceptorBindings.size() == 0) {
                return;
            }
            if (Beans.findInterceptorBindingConflicts(methodInterceptorBindings)) {
                throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType() + "." + method.getName() + "()");
            }

            initInterceptor(InterceptionType.AROUND_INVOKE, method, methodInterceptorBindings);
            initInterceptor(InterceptionType.AROUND_TIMEOUT, method, methodInterceptorBindings);
        }

        private void initInterceptor(InterceptionType interceptionType, WeldMethod<?, ?> method, Set<InterceptorBindingType> interceptorBindingTypes) {
            List<Interceptor<?>> methodBoundInterceptors = beanManager.resolveInterceptors(interceptionType, interceptorBindingTypes);
            if (methodBoundInterceptors != null && methodBoundInterceptors.size() > 0) {
                if (method.isFinal()) {
                    throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodBoundInterceptors.get(0).getBeanClass().getName());
                }
                Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();
                builder.intercept(interceptionType, javaMethod).with(toSerializableContextualArray(methodBoundInterceptors));
            }
        }

        private void initEjbInterceptors() {
            initClassDeclaredEjbInterceptors();
            for (WeldMethod<?, ?> method : businessMethods) {
                initMethodDeclaredEjbInterceptors(method);
            }
        }

        private void initClassDeclaredEjbInterceptors() {
            Class<?>[] classDeclaredInterceptors = null;
            if (getWeldAnnotated().isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass())) {
                Annotation interceptorsAnnotation = getType().getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass());
                classDeclaredInterceptors = SecureReflections.extractValues(interceptorsAnnotation);
            }

            if (classDeclaredInterceptors != null) {
                for (Class<?> clazz : classDeclaredInterceptors) {
                    builder.interceptAll().with(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
                }
            }
        }

        private void initMethodDeclaredEjbInterceptors(WeldMethod<?, ?> method) {
            Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();

            boolean excludeClassInterceptors = method.isAnnotationPresent(InterceptionUtils.getExcludeClassInterceptorsAnnotationClass());
            if (excludeClassInterceptors) {
                builder.ignoreGlobalInterceptors(javaMethod);
            }

            Class<?>[] methodDeclaredInterceptors = getMethodDeclaredInterceptors(method);
            if (methodDeclaredInterceptors != null && methodDeclaredInterceptors.length > 0) {
                if (method.isFinal()) {
                    throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodDeclaredInterceptors[0].getName());
                }

                InterceptionType interceptionType = isTimeoutAnnotationPresentOn(method)
                        ? InterceptionType.AROUND_TIMEOUT
                        : InterceptionType.AROUND_INVOKE;
                InterceptorMetadata[] interceptors = getMethodDeclaredInterceptorMetadatas(methodDeclaredInterceptors);
                builder.intercept(interceptionType, javaMethod).with(interceptors);
            }
        }

        private InterceptorMetadata[] getMethodDeclaredInterceptorMetadatas(Class<?>[] methodDeclaredInterceptors) {
            List<InterceptorMetadata<?>> list = new ArrayList<InterceptorMetadata<?>>();
            for (Class<?> clazz : methodDeclaredInterceptors) {
                list.add(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
            }
            return list.toArray(new InterceptorMetadata[list.size()]);
        }

        private boolean isTimeoutAnnotationPresentOn(WeldMethod<?, ?> method) {
            return method.isAnnotationPresent(beanManager.getServices().get(EJBApiAbstraction.class).TIMEOUT_ANNOTATION_CLASS);
        }

        private Class<?>[] getMethodDeclaredInterceptors(WeldMethod<?, ?> method) {
            Class<?>[] methodDeclaredInterceptors = null;
            if (method.isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass())) {
                methodDeclaredInterceptors = SecureReflections.extractValues(method.getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass()));
            }
            return methodDeclaredInterceptors;
        }

        private InterceptorMetadata<SerializableContextual<?, ?>>[] toSerializableContextualArray(List<Interceptor<?>> interceptors) {
            List<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> serializableContextuals = new ArrayList<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();
            for (Interceptor<?> interceptor : interceptors) {
                serializableContextuals.add(getCachedInterceptorMetadata(interceptor));
            }
            return serializableContextuals.toArray(AbstractClassBean.<SerializableContextual<?, ?>>emptyInterceptorMetadataArray());
        }

        private InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> getCachedInterceptorMetadata(Interceptor<?> interceptor) {
            InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> interceptorMetadata = interceptorMetadatas.get(interceptor);
            if (interceptorMetadata == null) {
                interceptorMetadata = getInterceptorMetadata(interceptor);
                interceptorMetadatas.put(interceptor, interceptorMetadata);
            }
            return interceptorMetadata;
        }

        private InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> getInterceptorMetadata(Interceptor<?> interceptor) {
            SerializableContextualImpl<Interceptor<?>, ?> contextual = new SerializableContextualImpl(getBeanManager().getContextId(), interceptor, getContextualStore());
            if (interceptor instanceof InterceptorImpl) {
                InterceptorImpl interceptorImpl = (InterceptorImpl) interceptor;
                WeldInterceptorClassMetadata classMetadata = WeldInterceptorClassMetadata.of(interceptorImpl.getWeldAnnotated());
                SerializableContextualInterceptorReference interceptorReference = new SerializableContextualInterceptorReference(contextual, classMetadata);
                return beanManager.getInterceptorMetadataReader().getInterceptorMetadata(interceptorReference);
            } else {
                //custom interceptor
                ClassMetadata<?> classMetadata = beanManager.getInterceptorMetadataReader().getClassMetadata(interceptor.getBeanClass());
                return new CustomInterceptorMetadata(new SerializableContextualInterceptorReference(contextual, null), classMetadata);
            }
        }
    }
}
