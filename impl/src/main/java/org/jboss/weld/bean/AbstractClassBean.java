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

import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;
import static org.jboss.weld.util.Interceptors.filterInterceptorBindings;
import static org.jboss.weld.util.Interceptors.flattenInterceptorBindings;
import static org.jboss.weld.util.Interceptors.mergeBeanInterceptorBindings;
import static org.jboss.weld.util.collections.WeldCollections.immutableList;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedConstructorImpl;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.bean.interceptor.CustomInterceptorMetadata;
import org.jboss.weld.bean.interceptor.SerializableContextualInterceptorReference;
import org.jboss.weld.bean.interceptor.WeldInterceptorClassMetadata;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.InterceptedSubclassFactory;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.ProxyObject;
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
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.interceptor.builder.InterceptionModelBuilder;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * An abstract bean representation common for class-based beans
 *
 * @param <T> the type of class for the bean
 * @author Pete Muir
 * @author David Allen
 * @author Jozef Hartinger
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>> {

    private static final InterceptorMetadata<?>[] EMPTY_INTERCEPTOR_METADATA_ARRAY = new InterceptorMetadata[0];

    private static <T> InterceptorMetadata<T>[] emptyInterceptorMetadataArray() {
        return cast(EMPTY_INTERCEPTOR_METADATA_ARRAY);
    }

    /**
     * Merges bean interceptor bindings (including inherited ones) with method interceptor bindings. Method interceptor bindings
     * override bean interceptor bindings. The bean binding map is not modified - a copy is used.
     */
    protected Map<Class<? extends Annotation>, Annotation> mergeMethodInterceptorBindings(Map<Class<? extends Annotation>, Annotation> beanBindings,
            Collection<Annotation> methodBindingAnnotations) {

        Map<Class<? extends Annotation>, Annotation> mergedBeanBindings = new HashMap<Class<? extends Annotation>, Annotation>(beanBindings);
        // conflict detection
        Set<Class<? extends Annotation>> processedBindingTypes = new HashSet<Class<? extends Annotation>>();

        for (Annotation methodBinding : methodBindingAnnotations) {
            Class<? extends Annotation> methodBindingType = methodBinding.annotationType();
            if (processedBindingTypes.contains(methodBindingType)) {
                throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType());
            }
            processedBindingTypes.add(methodBindingType);
            // override bean interceptor binding
            mergedBeanBindings.put(methodBindingType, methodBinding);
        }
        return mergedBeanBindings;
    }

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

    // Interceptors
    private boolean hasSerializationOrInvocationInterceptorMethods;

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
        initTargetClassInterceptors();
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
            new InterceptionModelInitializer().init();
        }
        hasInterceptors = this.isInterceptionCandidate() && (hasSerializationOrInvocationInterceptorMethods || beanManager.getInterceptorModelRegistry().get(getType()) != null);
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

    private void initTargetClassInterceptors() {
        if (!Beans.isInterceptor(getEnhancedAnnotated())) {
            InterceptorMetadata<T> interceptorClassMetadata = beanManager.getInterceptorMetadataReader().getTargetClassInterceptorMetadata(WeldInterceptorClassMetadata.of(getEnhancedAnnotated()));
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

    private class InterceptionModelInitializer {

        private Map<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> interceptorMetadatas = new HashMap<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();

        private List<AnnotatedMethod<?>> businessMethods;
        private InterceptionModelBuilder<ClassMetadata<?>,?> builder;

        public void init() {
            businessMethods = Beans.getInterceptableMethods(getEnhancedAnnotated());
            builder = InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(getClassMetadata());

            initCdiInterceptors();
            initEjbInterceptors();

            InterceptionModel<ClassMetadata<?>, ?> interceptionModel = builder.build();
            if (interceptionModel.getAllInterceptors().size() > 0 || hasSerializationOrInvocationInterceptorMethods) {
                if (getEnhancedAnnotated().isFinal()) {
                    throw new DefinitionException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, AbstractClassBean.this);
                }
                beanManager.getInterceptorModelRegistry().put(getType(), interceptionModel);
            }
        }

        private ClassMetadata<T> getClassMetadata() {
            return beanManager.getInterceptorMetadataReader().getClassMetadata(getType());
        }

        private void initCdiInterceptors() {
            Map<Class<? extends Annotation>, Annotation> classBindingAnnotations = getClassInterceptorBindings();
            initCdiLifecycleInterceptors(classBindingAnnotations);
            initCdiBusinessMethodInterceptors(classBindingAnnotations);
        }

        private Map<Class<? extends Annotation>, Annotation> getClassInterceptorBindings() {
            return mergeBeanInterceptorBindings(beanManager, getEnhancedAnnotated(), getStereotypes());
        }

        private void initCdiLifecycleInterceptors(Map<Class<? extends Annotation>, Annotation> classBindingAnnotations) {
            if (classBindingAnnotations.size() == 0) {
                return;
            }
            initLifeCycleInterceptor(InterceptionType.POST_CONSTRUCT, classBindingAnnotations);
            initLifeCycleInterceptor(InterceptionType.PRE_DESTROY, classBindingAnnotations);
            initLifeCycleInterceptor(InterceptionType.PRE_PASSIVATE, classBindingAnnotations);
            initLifeCycleInterceptor(InterceptionType.POST_ACTIVATE, classBindingAnnotations);
        }

        private void initLifeCycleInterceptor(InterceptionType interceptionType, Map<Class<? extends Annotation>, Annotation> classBindingAnnotations) {
            List<Interceptor<?>> resolvedInterceptors = beanManager.resolveInterceptors(interceptionType, classBindingAnnotations.values());
            if (!resolvedInterceptors.isEmpty()) {
                builder.intercept(interceptionType).with(toSerializableContextualArray(resolvedInterceptors));
            }
        }

        private void initCdiBusinessMethodInterceptors(Map<Class<? extends Annotation>, Annotation> classBindingAnnotations) {
            for (AnnotatedMethod<?> method : businessMethods) {
                initCdiBusinessMethodInterceptor(method, getMethodBindingAnnotations(classBindingAnnotations, method));
            }
        }

        private Collection<Annotation> getMethodBindingAnnotations(Map<Class<? extends Annotation>, Annotation> classBindingAnnotations, AnnotatedMethod<?> method) {
            Set<Annotation> methodBindingAnnotations = flattenInterceptorBindings(beanManager, filterInterceptorBindings(beanManager, method.getAnnotations()), true, true);
            return mergeMethodInterceptorBindings(classBindingAnnotations, methodBindingAnnotations).values();
        }

        private void initCdiBusinessMethodInterceptor(AnnotatedMethod<?> method, Collection<Annotation> methodBindingAnnotations) {
            if (methodBindingAnnotations.size() == 0) {
                return;
            }
            initInterceptor(InterceptionType.AROUND_INVOKE, method, methodBindingAnnotations);
            initInterceptor(InterceptionType.AROUND_TIMEOUT, method, methodBindingAnnotations);
        }

        private void initInterceptor(InterceptionType interceptionType, AnnotatedMethod<?> method, Collection<Annotation> methodBindingAnnotations) {
            List<Interceptor<?>> methodBoundInterceptors = beanManager.resolveInterceptors(interceptionType, methodBindingAnnotations);
            if (methodBoundInterceptors != null && methodBoundInterceptors.size() > 0) {
                if (Reflections.isFinal(method.getJavaMember())) {
                    throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodBoundInterceptors.get(0).getBeanClass().getName());
                }
                Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();
                builder.intercept(interceptionType, javaMethod).with(toSerializableContextualArray(methodBoundInterceptors));
            }
        }

        private void initEjbInterceptors() {
            initClassDeclaredEjbInterceptors();
            for (AnnotatedMethod<?> method : businessMethods) {
                initMethodDeclaredEjbInterceptors(method);
            }
        }

        private void initClassDeclaredEjbInterceptors() {
            Class<?>[] classDeclaredInterceptors = null;
            if (getEnhancedAnnotated().isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass())) {
                Annotation interceptorsAnnotation = getType().getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass());
                classDeclaredInterceptors = SecureReflections.extractValues(interceptorsAnnotation);
            }

            if (classDeclaredInterceptors != null) {
                for (Class<?> clazz : classDeclaredInterceptors) {
                    builder.interceptAll().with(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
                }
            }
        }

        private void initMethodDeclaredEjbInterceptors(AnnotatedMethod<?> method) {
            Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();

            boolean excludeClassInterceptors = method.isAnnotationPresent(InterceptionUtils.getExcludeClassInterceptorsAnnotationClass());
            if (excludeClassInterceptors) {
                builder.ignoreGlobalInterceptors(javaMethod);
            }

            Class<?>[] methodDeclaredInterceptors = getMethodDeclaredInterceptors(method);
            if (methodDeclaredInterceptors != null && methodDeclaredInterceptors.length > 0) {
                if (Reflections.isFinal(method.getJavaMember())) {
                    throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodDeclaredInterceptors[0].getName());
                }

                InterceptionType interceptionType = isTimeoutAnnotationPresentOn(method)
                        ? InterceptionType.AROUND_TIMEOUT
                        : InterceptionType.AROUND_INVOKE;
                InterceptorMetadata<?>[] interceptors = getMethodDeclaredInterceptorMetadatas(methodDeclaredInterceptors);
                builder.intercept(interceptionType, javaMethod).with(interceptors);
            }
        }

        private InterceptorMetadata<?>[] getMethodDeclaredInterceptorMetadatas(Class<?>[] methodDeclaredInterceptors) {
            List<InterceptorMetadata<?>> list = new ArrayList<InterceptorMetadata<?>>();
            for (Class<?> clazz : methodDeclaredInterceptors) {
                list.add(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
            }
            return list.toArray(new InterceptorMetadata[list.size()]);
        }

        private boolean isTimeoutAnnotationPresentOn(AnnotatedMethod<?> method) {
            return method.isAnnotationPresent(beanManager.getServices().get(EJBApiAbstraction.class).TIMEOUT_ANNOTATION_CLASS);
        }

        private Class<?>[] getMethodDeclaredInterceptors(AnnotatedMethod<?> method) {
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
            SerializableContextualImpl<Interceptor<?>, ?> contextual = new SerializableContextualImpl(interceptor, getContextualStore());
            if (interceptor instanceof InterceptorImpl) {
                InterceptorImpl<?> interceptorImpl = (InterceptorImpl<?>) interceptor;
                ClassMetadata<?> classMetadata = interceptorImpl.getInterceptorMetadata().getInterceptorClass();
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
