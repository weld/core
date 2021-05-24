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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeDisposerResolver;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.SetMultimap;

public class BeanDeployerEnvironment {

    private final Set<SlimAnnotatedTypeContext<?>> annotatedTypes;
    private final Set<Class<?>> vetoedClasses;
    private final SetMultimap<Class<?>, AbstractClassBean<?>> classBeanMap;
    private final SetMultimap<WeldMethodKey, ProducerMethod<?, ?>> producerMethodBeanMap;
    private final Set<ProducerField<?, ?>> producerFields;
    private final Set<RIBean<?>> beans;
    private final Set<ObserverInitializationContext<?, ?>> observers;
    private final Set<DisposalMethod<?, ?>> allDisposalBeans;
    private final Set<DisposalMethod<?, ?>> resolvedDisposalBeans;
    private final Set<DecoratorImpl<?>> decorators;
    private final Set<InterceptorImpl<?>> interceptors;
    private final TypeSafeDisposerResolver disposalMethodResolver;
    private final Set<Type> newBeanTypes;
    private final BeanManagerImpl manager;

    protected BeanDeployerEnvironment(BeanManagerImpl manager) {
        this(
                new HashSet<SlimAnnotatedTypeContext<?>>(),
                new HashSet<Class<?>>(),
                SetMultimap.<Class<?>, AbstractClassBean<?>>newConcurrentSetMultimap(),
                new HashSet<ProducerField<?, ?>>(),
                SetMultimap.<WeldMethodKey, ProducerMethod<?, ?>>newConcurrentSetMultimap(),
                new HashSet<RIBean<?>>(),
                new HashSet<ObserverInitializationContext<?, ?>>(),
                new HashSet<DisposalMethod<?, ?>>(),
                new HashSet<DisposalMethod<?, ?>>(),
                new HashSet<DecoratorImpl<?>>(),
                new HashSet<InterceptorImpl<?>>(),
                new HashSet<Type>(),
                manager);
    }

    protected BeanDeployerEnvironment(
            Set<SlimAnnotatedTypeContext<?>> annotatedTypes,
            Set<Class<?>> vetoedClasses,
            SetMultimap<Class<?>, AbstractClassBean<?>> classBeanMap,
            Set<ProducerField<?, ?>> producerFields,
            SetMultimap<WeldMethodKey, ProducerMethod<?, ?>> producerMethodBeanMap,
            Set<RIBean<?>> beans,
            Set<ObserverInitializationContext<?, ?>> observers,
            Set<DisposalMethod<?, ?>> allDisposalBeans,
            Set<DisposalMethod<?, ?>> resolvedDisposalBeans,
            Set<DecoratorImpl<?>> decorators,
            Set<InterceptorImpl<?>> interceptors,
            Set<Type> newBeanTypes,
            BeanManagerImpl manager) {
        this.annotatedTypes = annotatedTypes;
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
        this.disposalMethodResolver = new TypeSafeDisposerResolver(allDisposalBeans, manager.getServices().get(WeldConfiguration.class));
        this.newBeanTypes = newBeanTypes;
        this.manager = manager;
    }

    public void addAnnotatedType(SlimAnnotatedTypeContext<?> annotatedType) {
        this.annotatedTypes.add(annotatedType);
    }

    public void addAnnotatedTypes(Collection<SlimAnnotatedTypeContext<?>> annotatedTypes) {
        this.annotatedTypes.addAll(annotatedTypes);
    }

    public void addSyntheticAnnotatedType(SlimAnnotatedType<?> annotatedType, Extension extension) {
        addAnnotatedType(SlimAnnotatedTypeContext.of(annotatedType, extension));
    }

    public Set<SlimAnnotatedTypeContext<?>> getAnnotatedTypes() {
        return Collections.unmodifiableSet(annotatedTypes);
    }

    public void removeAnnotatedType(SlimAnnotatedTypeContext<?> annotatedType) {
        annotatedTypes.remove(annotatedType);
    }

    public void removeAnnotatedTypes(Collection<SlimAnnotatedTypeContext<?>> annotatedTypes) {
        this.annotatedTypes.removeAll(annotatedTypes);
    }

    public void vetoJavaClass(Class<?> javaClass) {
        vetoedClasses.add(javaClass);
    }

    public boolean isVetoed(Class<?> clazz) {
        return vetoedClasses.contains(clazz);
    }

    public Set<ProducerMethod<?, ?>> getProducerMethod(Class<?> declaringClass, MethodSignature signature) {
        WeldMethodKey key = new WeldMethodKey(declaringClass, signature);
        Set<ProducerMethod<?, ?>> beans = producerMethodBeanMap.get(key);
        for (ProducerMethod<?, ?> producerMethod : beans) {
            producerMethod.initialize(this);
        }
        return beans;
    }

    public Set<AbstractClassBean<?>> getClassBeans(Class<?> clazz) {
        Set<AbstractClassBean<?>> beans = classBeanMap.get(clazz);
        for (AbstractClassBean<?> bean : beans) {
            bean.preInitialize();
        }
        return beans;
    }

    public void addProducerMethod(ProducerMethod<?, ?> bean) {
        producerMethodBeanMap.get(WeldMethodKey.of(bean)).add(bean);
        addAbstractBean(bean);
    }

    public void addProducerField(ProducerField<?, ?> bean) {
        producerFields.add(bean);
        addAbstractBean(bean);
    }

    public void addExtension(ExtensionBean<?> bean) {
        beans.add(bean);
    }

    public void addBuiltInBean(AbstractBuiltInBean<?> bean) {
        beans.add(bean);
    }

    protected void addAbstractClassBean(AbstractClassBean<?> bean) {
        classBeanMap.get(bean.getBeanClass()).add(bean);
        addAbstractBean(bean);
    }

    public void addManagedBean(ManagedBean<?> bean) {
        addAbstractClassBean(bean);
    }

    public void addSessionBean(SessionBean<?> bean) {
        Preconditions.checkArgument(bean instanceof AbstractClassBean<?>, bean);
        addAbstractClassBean((AbstractClassBean<?>) bean);
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
    }

    public void addObserverMethod(ObserverInitializationContext<?, ?> observerInitializer) {
        this.observers.add(observerInitializer);
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

    protected static class WeldMethodKey {

        static WeldMethodKey of(ProducerMethod<?, ?> producerMethod) {
            return new WeldMethodKey(producerMethod.getBeanClass(), producerMethod.getEnhancedAnnotated().getSignature());
        }

        private final Class<?> declaringClass;
        private final MethodSignature signature;

        WeldMethodKey(Class<?> clazz, MethodSignature signature) {
            this.declaringClass = clazz;
            this.signature = signature;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
            result = prime * result + ((signature == null) ? 0 : signature.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            WeldMethodKey other = (WeldMethodKey) obj;
            if (declaringClass == null) {
                if (other.declaringClass != null) {
                    return false;
                }
            } else if (!declaringClass.equals(other.declaringClass)) {
                return false;
            }
            if (signature == null) {
                if (other.signature != null) {
                    return false;
                }
            } else if (!signature.equals(other.signature)) {
                return false;
            }
            return true;
        }
    }

    public void vetoBean(AbstractBean<?, ?> bean) {
        beans.remove(bean);
        if (bean instanceof AbstractClassBean<?>) {
            classBeanMap.get(bean.getBeanClass()).remove(bean);
            if (bean instanceof InterceptorImpl<?>) {
                interceptors.remove(bean);
            }
            if (bean instanceof DecoratorImpl<?>) {
                decorators.remove(bean);
            }
        }
        if (bean instanceof ProducerMethod<?, ?>) {
            ProducerMethod<?, ?> producerMethod = cast(bean);
            producerMethodBeanMap.get(WeldMethodKey.of(producerMethod)).remove(producerMethod);
        }
        if (bean instanceof ProducerField<?, ?>) {
            producerFields.remove(bean);
        }
    }

    public Iterable<AbstractClassBean<?>> getClassBeans() {
        return classBeanMap.values();
    }

    public Iterable<ProducerMethod<?, ?>> getProducerMethodBeans() {
        return producerMethodBeanMap.values();
    }

    public Set<ProducerField<?, ?>> getProducerFields() {
        return Collections.unmodifiableSet(producerFields);
    }

    public void cleanup() {
        this.annotatedTypes.clear();
        this.vetoedClasses.clear();
        this.classBeanMap.clear();
        this.producerMethodBeanMap.clear();
        this.producerFields.clear();
        this.allDisposalBeans.clear();
        this.resolvedDisposalBeans.clear();
        this.beans.clear();
        this.decorators.clear();
        this.interceptors.clear();
        this.observers.clear();
        this.disposalMethodResolver.clear();
        this.newBeanTypes.clear();
    }

    public Set<Type> getNewBeanTypes() {
        return Collections.unmodifiableSet(newBeanTypes);
    }

    public void trim() {
        for (Iterator<SlimAnnotatedTypeContext<?>> iterator = annotatedTypes.iterator(); iterator.hasNext(); ) {
            if (!AnnotatedTypes.hasBeanDefiningAnnotation(iterator.next().getAnnotatedType(), AnnotatedTypes.TRIM_META_ANNOTATIONS)) {
                iterator.remove();
            }
        }
    }
}
