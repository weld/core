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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InterceptorBindingSet;
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

    private InterceptorMetadata<SerializableContextual<?, ?>>[] toSerializableContextualArray(List<Interceptor<?>> interceptors) {
        List<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> serializableContextuals = new ArrayList<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();
        for (Interceptor<?> interceptor : interceptors) {

            SerializableContextualImpl<Interceptor<?>, ?> contextual = new SerializableContextualImpl(interceptor, getServices().get(ContextualStore.class));
            if (interceptor instanceof InterceptorImpl) {
                serializableContextuals.add(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(new SerializableContextualInterceptorReference(contextual, WeldInterceptorClassMetadata.of(((InterceptorImpl) interceptor).getWeldAnnotated()))));
            } else {
                //custom interceptor
                serializableContextuals.add(new CustomInterceptorMetadata(new SerializableContextualInterceptorReference(contextual, null), beanManager.getInterceptorMetadataReader().getClassMetadata(interceptor.getBeanClass())));
            }
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

    protected void initInterceptionModelForType() {
        ClassMetadata<?> classMetadata = beanManager.getInterceptorMetadataReader().getClassMetadata(getType());

        InterceptionModelBuilder<ClassMetadata<?>, ?> builder = InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(classMetadata);

        // initialize CDI interceptors
        Set<Annotation> classBindingAnnotations = new InterceptorBindingSet(beanManager);
        classBindingAnnotations.addAll(beanManager.extractInterceptorBindings(getWeldAnnotated().getAnnotations()));
        for (Class<? extends Annotation> annotation : getStereotypes()) {
            classBindingAnnotations.addAll(beanManager.extractInterceptorBindings(beanManager.getStereotypeDefinition(annotation)));
        }
        if (classBindingAnnotations.size() > 0) {
            if (Beans.findInterceptorBindingConflicts(beanManager, classBindingAnnotations)) {
                throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType());
            }

            Annotation[] classBindingAnnotationsArray = classBindingAnnotations.toArray(new Annotation[classBindingAnnotations.size()]);

            List<Interceptor<?>> resolvedPostConstructInterceptors = beanManager.resolveInterceptors(InterceptionType.POST_CONSTRUCT, classBindingAnnotationsArray);
            builder.interceptPostConstruct().with(toSerializableContextualArray(resolvedPostConstructInterceptors));

            List<Interceptor<?>> resolvedPreDestroyInterceptors = beanManager.resolveInterceptors(InterceptionType.PRE_DESTROY, classBindingAnnotationsArray);
            builder.interceptPreDestroy().with(toSerializableContextualArray(resolvedPreDestroyInterceptors));

            List<Interceptor<?>> resolvedPrePassivateInterceptors = beanManager.resolveInterceptors(InterceptionType.PRE_PASSIVATE, classBindingAnnotationsArray);
            builder.interceptPrePassivate().with(toSerializableContextualArray(resolvedPrePassivateInterceptors));

            List<Interceptor<?>> resolvedPostActivateInterceptors = beanManager.resolveInterceptors(InterceptionType.POST_ACTIVATE, classBindingAnnotationsArray);
            builder.interceptPostActivate().with(toSerializableContextualArray(resolvedPostActivateInterceptors));

        }
        List<WeldMethod<?, ?>> businessMethods = Beans.getInterceptableMethods(getWeldAnnotated());
        for (WeldMethod<?, ?> method : businessMethods) {
            Set<Annotation> methodBindingAnnotations = new InterceptorBindingSet(beanManager);
            methodBindingAnnotations.addAll(classBindingAnnotations);
            methodBindingAnnotations.addAll(beanManager.extractInterceptorBindings(method.getAnnotations()));
            if (methodBindingAnnotations.size() > 0) {
                if (Beans.findInterceptorBindingConflicts(beanManager, methodBindingAnnotations)) {
                    throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType() + "." + method.getName() + "()");
                }

                Annotation[] methodBindingAnnotationsArray = methodBindingAnnotations.toArray(new Annotation[methodBindingAnnotations.size()]);

                List<Interceptor<?>> methodBoundInterceptors = beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE, methodBindingAnnotationsArray);
                if (methodBoundInterceptors != null && methodBoundInterceptors.size() > 0) {
                    if (method.isFinal()) {
                        throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodBoundInterceptors.get(0).getBeanClass().getName());
                    }
                    builder.interceptAroundInvoke(Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember()).with(toSerializableContextualArray(methodBoundInterceptors));
                }

                methodBoundInterceptors = beanManager.resolveInterceptors(InterceptionType.AROUND_TIMEOUT, methodBindingAnnotationsArray);
                if (methodBoundInterceptors != null && methodBoundInterceptors.size() > 0) {
                    if (method.isFinal()) {
                        throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodBoundInterceptors.get(0).getBeanClass().getName());
                    }
                    builder.interceptAroundTimeout(Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember()).with(toSerializableContextualArray(methodBoundInterceptors));
                }

            }
        }

        // initialize EJB3 interceptors
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

        for (WeldMethod<?, ?> method : businessMethods) {
            boolean excludeClassInterceptors = method.isAnnotationPresent(InterceptionUtils.getExcludeClassInterceptorsAnnotationClass());
            Class<?>[] methodDeclaredInterceptors = null;
            if (method.isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass())) {
                methodDeclaredInterceptors = SecureReflections.extractValues(method.getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass()));
            }
            if (excludeClassInterceptors) {
                builder.ignoreGlobalInterceptors(Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember());
            }
            if (methodDeclaredInterceptors != null && methodDeclaredInterceptors.length > 0) {
                List<InterceptorMetadata<?>> methodDeclaredInterceptorMetadatas = new ArrayList<InterceptorMetadata<?>>();
                for (Class<?> clazz : methodDeclaredInterceptors) {
                    methodDeclaredInterceptorMetadatas.add(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
                }
                if (method.isFinal()) {
                    throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodDeclaredInterceptors[0].getName());
                }
                if (method.isAnnotationPresent(beanManager.getServices().get(EJBApiAbstraction.class).TIMEOUT_ANNOTATION_CLASS)) {
                    builder.interceptAroundTimeout(Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember()).with(methodDeclaredInterceptorMetadatas.toArray(new InterceptorMetadata[]{}));
                } else {
                    builder.interceptAroundInvoke(Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember()).with(methodDeclaredInterceptorMetadatas.toArray(new InterceptorMetadata[]{}));
                }
            }
        }
        InterceptionModel<ClassMetadata<?>, ?> interceptionModel = builder.build();

        if (interceptionModel.getAllInterceptors().size() > 0 || hasSerializationOrInvocationInterceptorMethods) {
            if (getWeldAnnotated().isFinal()) {
                throw new DefinitionException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, this);
            }
            beanManager.getInterceptorModelRegistry().put(getType(), ((InterceptionModel<ClassMetadata<?>, ?>) interceptionModel));
        }


    }

    @Override
    public void initializeAfterBeanDiscovery() {
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(getType())) {
            initInterceptionModelForType();
        }
        initDecorators();
        super.initializeAfterBeanDiscovery();
        if (isSubclassed()) {
            initEnhancedSubclass();
        }
        if(hasDecorators()) {
            decoratorProxyFactory = new ProxyFactory<T>(getType(), getTypes(), this);
            decoratorProxyFactory.getProxyClass(); //eagerly generate the proxy class
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
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanInstance, this, decoratorProxyFactory.getProxyClass(), beanManager, getServices().get(ContextualStore.class), decorators);
        DecorationHelper.getHelperStack().push(decorationHelper);
        final T outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
        DecorationHelper.getHelperStack().pop();
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
        injectableFields = Beans.getFieldInjectionPoints(this, annotatedItem);
        addInjectionPoints(Beans.getFieldInjectionPoints(this, injectableFields));
    }

    /**
     * Initializes the initializer methods
     */
    protected void initInitializerMethods() {
        initializerMethods = Beans.getInitializerMethods(this, getWeldAnnotated());
        addInjectionPoints(Beans.getParameterInjectionPoints(this, initializerMethods));
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
        return this.isInterceptionCandidate() && (hasSerializationOrInvocationInterceptorMethods || beanManager.getInterceptorModelRegistry().get(getType()) != null);
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
        this.constructor = Beans.getBeanConstructor(this, getWeldAnnotated());
        addInjectionPoints(Beans.getParameterInjectionPoints(this, constructor));
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
        return !Reflections.isFinal(getType()) && (hasDecorators() || hasInterceptors());
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

}
