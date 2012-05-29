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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeDisposerResolver;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.Sets;

public class BeanDeployerEnvironment {

    public static BeanDeployerEnvironment newEnvironment(EjbDescriptors ejbDescriptors, BeanManagerImpl manager) {
        return new BeanDeployerEnvironment(ejbDescriptors, manager);
    }

    /**
     * Creates a new threadsafe BeanDeployerEnvironment instance. These instances are used by {@link ConcurrentBeanDeployer} during bootstrap.
     */
    public static BeanDeployerEnvironment newConcurrentEnvironment(EjbDescriptors ejbDescriptors, BeanManagerImpl manager) {
        return new BeanDeployerEnvironment(
                Sets.newSetFromMap(new ConcurrentHashMap<AnnotatedType<?>, Boolean>()),
                new ConcurrentHashMap<AnnotatedType<?>, Extension>(),
                Sets.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>()),
                new ConcurrentHashMap<AnnotatedType<?>, AbstractClassBean<?>>(),
                Sets.newSetFromMap(new ConcurrentHashMap<ProducerField<?, ?>, Boolean>()),
                new ConcurrentHashMap<WeldMethodKey<?, ?>, ProducerMethod<?, ?>>(),
                Sets.newSetFromMap(new ConcurrentHashMap<RIBean<?>, Boolean>()),
                Sets.newSetFromMap(new ConcurrentHashMap<ObserverInitializationContext<?, ?>, Boolean>()),
                Sets.newSetFromMap(new ConcurrentHashMap<DisposalMethod<?, ?>, Boolean>()),
                Sets.newSetFromMap(new ConcurrentHashMap<DisposalMethod<?, ?>, Boolean>()),
                Sets.newSetFromMap(new ConcurrentHashMap<DecoratorImpl<?>, Boolean>()),
                Sets.newSetFromMap(new ConcurrentHashMap<InterceptorImpl<?>, Boolean>()),
                ejbDescriptors,
                Sets.newSetFromMap(new ConcurrentHashMap<EnhancedAnnotatedType<?>, Boolean>()),
                new ConcurrentHashMap<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>>(),
                manager);
    }

    private final Set<AnnotatedType<?>> annotatedTypes;
    private final Map<AnnotatedType<?>, Extension> annotatedTypeSource;
    private final Set<Class<?>> vetoedClasses;
    private final Map<AnnotatedType<?>, AbstractClassBean<?>> classBeanMap;
    private final Map<WeldMethodKey<?, ?>, ProducerMethod<?, ?>> producerMethodBeanMap;
    private final Set<ProducerField<?, ?>> producerFields;
    private final Set<RIBean<?>> beans;
    private final Set<ObserverInitializationContext<?, ?>> observers;
    private final Set<DisposalMethod<?, ?>> allDisposalBeans;
    private final Set<DisposalMethod<?, ?>> resolvedDisposalBeans;
    private final Set<DecoratorImpl<?>> decorators;
    private final Set<InterceptorImpl<?>> interceptors;
    private final EjbDescriptors ejbDescriptors;
    private final TypeSafeDisposerResolver disposalMethodResolver;
    private final ClassTransformer classTransformer;
    private final Set<EnhancedAnnotatedType<?>> newManagedBeanClasses;
    private final Map<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>> newSessionBeanDescriptorsFromInjectionPoint;
    private final BeanManagerImpl manager;

    protected BeanDeployerEnvironment(
            Set<AnnotatedType<?>> annotatedTypes,
            Map<AnnotatedType<?>, Extension> annotatedTypeSource,
            Set<Class<?>> vetoedClasses,
            Map<AnnotatedType<?>, AbstractClassBean<?>> classBeanMap,
            Set<ProducerField<?, ?>> producerFields,
            Map<WeldMethodKey<?, ?>, ProducerMethod<?, ?>> producerMethodBeanMap,
            Set<RIBean<?>> beans,
            Set<ObserverInitializationContext<?, ?>> observers,
            Set<DisposalMethod<?, ?>> allDisposalBeans,
            Set<DisposalMethod<?, ?>> resolvedDisposalBeans,
            Set<DecoratorImpl<?>> decorators,
            Set<InterceptorImpl<?>> interceptors,
            EjbDescriptors ejbDescriptors,
            Set<EnhancedAnnotatedType<?>> newManagedBeanClasses,
            Map<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>> newSessionBeanDescriptorsFromInjectionPoint,
            BeanManagerImpl manager) {
        this.annotatedTypes = annotatedTypes;
        this.annotatedTypeSource = annotatedTypeSource;
        this.vetoedClasses = vetoedClasses;
        this.classBeanMap = classBeanMap;
        this.producerFields = producerFields;
        this.producerMethodBeanMap = producerMethodBeanMap;
        this.beans = beans;
        this.observers = observers;
        this.allDisposalBeans = allDisposalBeans;
        this.resolvedDisposalBeans = resolvedDisposalBeans;
        this.decorators = decorators;
        this.interceptors = interceptors;
        this.ejbDescriptors = ejbDescriptors;
        this.disposalMethodResolver = new TypeSafeDisposerResolver(manager, allDisposalBeans);
        this.classTransformer = manager.getServices().get(ClassTransformer.class);
        this.newManagedBeanClasses = newManagedBeanClasses;
        this.newSessionBeanDescriptorsFromInjectionPoint = newSessionBeanDescriptorsFromInjectionPoint;
        this.manager = manager;
    }

    protected BeanDeployerEnvironment(EjbDescriptors ejbDescriptors, BeanManagerImpl manager) {
        this(
                new HashSet<AnnotatedType<?>>(),
                new HashMap<AnnotatedType<?>, Extension>(),
                new HashSet<Class<?>>(),
                new HashMap<AnnotatedType<?>, AbstractClassBean<?>>(),
                new HashSet<ProducerField<?, ?>>(),
                new HashMap<WeldMethodKey<?, ?>, ProducerMethod<?, ?>>(),
                new HashSet<RIBean<?>>(),
                new HashSet<ObserverInitializationContext<?, ?>>(),
                new HashSet<DisposalMethod<?, ?>>(),
                new HashSet<DisposalMethod<?, ?>>(),
                new HashSet<DecoratorImpl<?>>(),
                new HashSet<InterceptorImpl<?>>(),
                ejbDescriptors,
                new HashSet<EnhancedAnnotatedType<?>>(),
                new HashMap<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>>(),
                manager);
    }

    public void addAnnotatedType(AnnotatedType<?> annotatedType) {
        this.annotatedTypes.add(annotatedType);
    }

    public void addAnnotatedTypes(Collection<AnnotatedType<?>> annotatedTypes) {
        this.annotatedTypes.addAll(annotatedTypes);
    }

    public void addSyntheticAnnotatedType(SlimAnnotatedType<?> annotatedType, Extension extension) {
        addAnnotatedType(annotatedType);
        annotatedTypeSource.put(annotatedType, extension);
    }

    public Set<AnnotatedType<?>> getAnnotatedTypes() {
        return Collections.unmodifiableSet(annotatedTypes);
    }

    public Extension getAnnotatedTypeSource(AnnotatedType<?> annotatedType) {
        return annotatedTypeSource.get(annotatedType);
    }

    public void removeAnnotatedType(AnnotatedType<?> annotatedType) {
        annotatedTypes.remove(annotatedType);
    }

    public void removeAnnotatedTypes(Collection<AnnotatedType<?>> annotatedTypes) {
        for (AnnotatedType<?> annotatedType : annotatedTypes) {
            removeAnnotatedType(annotatedType);
        }
    }

    public void vetoJavaClass(Class<?> javaClass) {
        vetoedClasses.add(javaClass);
    }

    public void vetoAnnotatedType(AnnotatedType<?> annotatedType) {
        if (annotatedType instanceof BackedAnnotatedType<?>) {
            vetoJavaClass(annotatedType.getJavaClass());
        }
        removeAnnotatedType(annotatedType);
    }

    public boolean isVetoed(Class<?> clazz) {
        return vetoedClasses.contains(clazz);
    }

    public Set<EnhancedAnnotatedType<?>> getNewManagedBeanClasses() {
        return newManagedBeanClasses;
    }

    public Map<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>> getNewSessionBeanDescriptorsFromInjectionPoint() {
        return newSessionBeanDescriptorsFromInjectionPoint;
    }

    public <X, T> ProducerMethod<X, T> getProducerMethod(EnhancedAnnotatedMethod<X, T> method) {
        WeldMethodKey<X, T> key = new WeldMethodKey<X, T>(method);
        ProducerMethod<?, ?> bean = producerMethodBeanMap.get(key);
        if (bean != null) {
            bean.initialize(this);
        }
        return cast(bean);
    }

    public AbstractClassBean<?> getClassBean(AnnotatedType<?> clazz) {
        AnnotatedType<?> type = clazz;
        if (clazz instanceof EnhancedAnnotatedType<?>) {
            type = Reflections.<EnhancedAnnotatedType<?>>cast(clazz).slim();
        }
        AbstractClassBean<?> bean = classBeanMap.get(type);
        if (bean != null) {
            bean.preInitialize();
        }
        return bean;
    }

    public void addProducerMethod(ProducerMethod<?, ?> bean) {
        producerMethodBeanMap.put(WeldMethodKey.of(bean.getEnhancedAnnotated()), bean);
        addAbstractBean(bean);
    }

    public void addProducerField(ProducerField<?, ?> bean) {
        producerFields.add(bean);
        addAbstractBean(bean);
    }

    public void addExtension(ExtensionBean bean) {
        beans.add(bean);
    }

    public void addBuiltInBean(AbstractBuiltInBean<?> bean) {
        beans.add(bean);
    }

    protected void addAbstractClassBean(AbstractClassBean<?> bean) {
        if (!(bean instanceof NewBean)) {
            classBeanMap.put(bean.getAnnotated(), bean);
        }
        addAbstractBean(bean);
    }

    public void addManagedBean(ManagedBean<?> bean) {
        addAbstractClassBean(bean);
    }

    public void addSessionBean(SessionBean<?> bean) {
        addAbstractClassBean(bean);
    }

    public void addNewManagedBean(NewManagedBean<?> bean) {
        beans.add(bean);
    }

    public void addNewSessionBean(NewSessionBean<?> bean) {
        beans.add(bean);
    }

    protected void addAbstractBean(AbstractBean<?, ?> bean) {
        beans.add(bean);
    }

    public void addDecorator(DecoratorImpl<?> bean) {
        decorators.add(bean);
    }

    public void addInterceptor(InterceptorImpl<?> bean) {
        interceptors.add(bean);
    }

    public void addDisposesMethod(DisposalMethod<?, ?> bean) {
        allDisposalBeans.add(bean);
        addNewBeansFromInjectionPoints(bean.getInjectionPoints());
    }

    public void addObserverMethod(ObserverInitializationContext<?, ?> observerInitializer) {
        this.observers.add(observerInitializer);
        addNewBeansFromInjectionPoints(observerInitializer.getObserver().getInjectionPoints());
    }

    public void addNewBeansFromInjectionPoints(AbstractBean<?, ?> bean) {
        addNewBeansFromInjectionPoints(bean.getInjectionPoints());
    }

    public void addNewBeansFromInjectionPoints(Set<? extends InjectionPoint> injectionPoints) {
        for (InjectionPoint injectionPoint : injectionPoints) {
            WeldInjectionPoint<?, ?> weldInjectionPoint = InjectionPoints.getWeldInjectionPoint(injectionPoint);
            if (weldInjectionPoint.getQualifier(New.class) != null) {

                Class<?> rawType = Reflections.getRawType(weldInjectionPoint.getType());
                if (Event.class.equals(rawType)) {
                    continue;
                }
                New _new = weldInjectionPoint.getQualifier(New.class);
                if (_new.value().equals(New.class)) {
                    if (rawType.equals(Instance.class)) {
                        // e.g. @Inject @New(ChequePaymentProcessor.class) Instance<PaymentProcessor> chequePaymentProcessor;
                        // see WELD-975
                        Type typeParameter = Reflections.getActualTypeArguments(weldInjectionPoint.getType())[0];
                        addNewBeanFromInjecitonPoint(Reflections.getRawType(typeParameter), typeParameter);
                    } else {
                        addNewBeanFromInjecitonPoint(rawType, weldInjectionPoint.getType());
                    }
                } else {
                    addNewBeanFromInjecitonPoint(_new.value(), _new.value());
                }
            }
        }
    }

    private void addNewBeanFromInjecitonPoint(Class<?> rawType, Type baseType) {
        if (getEjbDescriptors().contains(rawType)) {
            InternalEjbDescriptor<?> descriptor = getEjbDescriptors().getUnique(rawType);
            newSessionBeanDescriptorsFromInjectionPoint.put(descriptor, classTransformer.getEnhancedAnnotatedType(rawType, baseType));
        } else {
            newManagedBeanClasses.add(classTransformer.getEnhancedAnnotatedType(rawType, baseType));
        }
    }

    public Set<? extends RIBean<?>> getBeans() {
        return Collections.unmodifiableSet(beans);
    }

    public Set<DecoratorImpl<?>> getDecorators() {
        return Collections.unmodifiableSet(decorators);
    }

    public Set<InterceptorImpl<?>> getInterceptors() {
        return Collections.unmodifiableSet(interceptors);
    }

    public Set<ObserverInitializationContext<?, ?>> getObservers() {
        return Collections.unmodifiableSet(observers);
    }

    public Set<DisposalMethod<?, ?>> getUnresolvedDisposalBeans() {
        Set<DisposalMethod<?, ?>> beans = new HashSet<DisposalMethod<?, ?>>(allDisposalBeans);
        beans.removeAll(resolvedDisposalBeans);
        return Collections.unmodifiableSet(beans);
    }

    public EjbDescriptors getEjbDescriptors() {
        return ejbDescriptors;
    }

    /**
     * Resolve the disposal method for the given producer method. Any resolved
     * beans will be marked as such for the purpose of validating that all
     * disposal methods are used. For internal use.
     *
     * @param types the types
     * @param qualifiers The binding types to match
     * @param declaringBean declaring bean
     * @return The set of matching disposal methods
     */
    public <X> Set<DisposalMethod<X, ?>> resolveDisposalBeans(Set<Type> types, Set<Annotation> qualifiers, AbstractClassBean<X> declaringBean) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Set<DisposalMethod<X, ?>> beans = cast(disposalMethodResolver.resolve(new ResolvableBuilder(manager).addTypes(types).addQualifiers(qualifiers).setDeclaringBean(declaringBean).create(), true));
        resolvedDisposalBeans.addAll(beans);
        return Collections.unmodifiableSet(beans);
    }

    protected static class WeldMethodKey<T, X> {

        static <T, X> WeldMethodKey<T, X> of(EnhancedAnnotatedMethod<T, X> method) {
            return new WeldMethodKey<T, X>(method);
        }

        final EnhancedAnnotatedMethod<T, X> method;

        WeldMethodKey(EnhancedAnnotatedMethod<T, X> meth) {
            this.method = meth;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof WeldMethodKey<?, ?>) {
                WeldMethodKey<?, ?> o = (WeldMethodKey<?, ?>) other;
                return AnnotatedTypes.compareAnnotatedCallable(method, o.method);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return method.getJavaMember().hashCode();
        }
    }

    public void vetoBean(AbstractBean<?, ?> bean) {
        beans.remove(bean);
        if (bean instanceof AbstractClassBean<?>) {
            classBeanMap.remove(bean.getAnnotated());
            if (bean instanceof InterceptorImpl<?>) {
                interceptors.remove(bean);
            }
            if (bean instanceof DecoratorImpl<?>) {
                decorators.remove(bean);
            }
        }
        if (bean instanceof ProducerMethod<?, ?>) {
            producerMethodBeanMap.remove(WeldMethodKey.of(Reflections.<ProducerMethod<?, ?>>cast(bean).getEnhancedAnnotated()));
        }
        if (bean instanceof ProducerField<?, ?>) {
            producerFields.remove(bean);
        }
    }

    public Map<AnnotatedType<?>, AbstractClassBean<?>> getClassBeanMap() {
        return Collections.unmodifiableMap(classBeanMap);
    }

    public Map<WeldMethodKey<?, ?>, ProducerMethod<?, ?>> getProducerMethodBeanMap() {
        return Collections.unmodifiableMap(producerMethodBeanMap);
    }

    public Set<ProducerField<?, ?>> getProducerFields() {
        return Collections.unmodifiableSet(producerFields);
    }

    public void cleanup() {
        this.annotatedTypes.clear();
        this.annotatedTypeSource.clear();
        this.vetoedClasses.clear();
        this.classBeanMap.clear();
        this.producerMethodBeanMap.clear();
        this.allDisposalBeans.clear();
        this.resolvedDisposalBeans.clear();
        this.beans.clear();
        this.decorators.clear();
        this.interceptors.clear();
        this.observers.clear();
        this.disposalMethodResolver.clear();
        this.newManagedBeanClasses.clear();
        this.newSessionBeanDescriptorsFromInjectionPoint.clear();
    }
}
