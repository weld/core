/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessSyntheticAnnotatedType;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.event.ExtensionObserverMethodImpl;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ParameterInjectionPointAttributes;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.util.reflection.Reflections;

public class ContainerLifecycleEvents extends AbstractBootstrapService {

    private boolean everythingObserved;
    private boolean processAnnotatedTypeObserved;
    private boolean processBeanObserved;
    private boolean processBeanAttributesObserved;
    private boolean processInjectionPointObserved;
    private boolean processInjectionTargetObserved;
    private boolean processProducerObserved;
    private boolean processObserverMethodObserved;
    private final RequiredAnnotationDiscovery discovery;

    private final ContainerLifecycleEventPreloader preloader;

    public ContainerLifecycleEvents(ContainerLifecycleEventPreloader preloader, RequiredAnnotationDiscovery discovery) {
        this.preloader = preloader;
        this.discovery = discovery;
    }

    public void processObserverMethod(ObserverMethod<?> observer) {
        if (observer instanceof ContainerLifecycleEventObserverMethod) {
            processObserverMethodType(observer.getObservedType());
        }
    }

    protected void processObserverMethodType(Type observedType) {
        if (everythingObserved) {
            return;
        }

        Class<?> rawType = Reflections.getRawType(observedType);
        if (Object.class.equals(rawType)) {
            this.everythingObserved = true;
            this.processAnnotatedTypeObserved = true;
            this.processBeanObserved = true;
            this.processBeanAttributesObserved = true;
            this.processInjectionPointObserved = true;
            this.processInjectionTargetObserved = true;
            this.processProducerObserved = true;
            this.processObserverMethodObserved = true;
        } else if (!processAnnotatedTypeObserved && ProcessAnnotatedType.class.isAssignableFrom(rawType)) {
            processAnnotatedTypeObserved = true;
        } else if (!processBeanObserved && ProcessBean.class.isAssignableFrom(rawType)) {
            processBeanObserved = true;
        } else if (!processBeanAttributesObserved && ProcessBeanAttributes.class.isAssignableFrom(rawType)) {
            processBeanAttributesObserved = true;
        } else if (!processObserverMethodObserved && ProcessObserverMethod.class.isAssignableFrom(rawType)) {
            processObserverMethodObserved = true;
        } else if (!processProducerObserved && ProcessProducer.class.equals(rawType)) {
            processProducerObserved = true;
        } else if (!processInjectionTargetObserved && ProcessInjectionTarget.class.equals(rawType)) {
            processInjectionTargetObserved = true;
        } else if (!processInjectionPointObserved && ProcessInjectionPoint.class.equals(rawType)) {
            processInjectionPointObserved = true;
        }
    }

    public boolean isProcessAnnotatedTypeObserved() {
        return processAnnotatedTypeObserved;
    }

    public boolean isProcessBeanObserved() {
        return processBeanObserved;
    }

    public boolean isProcessBeanAttributesObserved() {
        return processBeanAttributesObserved;
    }

    public boolean isProcessObserverMethodObserved() {
        return processObserverMethodObserved;
    }

    public boolean isProcessProducerObserved() {
        return processProducerObserved;
    }

    public boolean isProcessInjectionTargetObserved() {
        return processInjectionTargetObserved;
    }

    public boolean isProcessInjectionPointObserved() {
        return processInjectionPointObserved;
    }

    public <T> ProcessAnnotatedTypeImpl<T> fireProcessAnnotatedType(BeanManagerImpl beanManager,
            SlimAnnotatedTypeContext<T> annotatedTypeContext) {
        if (!isProcessAnnotatedTypeObserved()) {
            return null;
        }
        final Set<ContainerLifecycleEventObserverMethod<?>> observers = annotatedTypeContext
                .getResolvedProcessAnnotatedTypeObservers();
        final SlimAnnotatedType<T> annotatedType = annotatedTypeContext.getAnnotatedType();
        // if the fast resolver resolved an empty set of observer methods, skip this event
        if (observers != null && observers.isEmpty()) {
            BootstrapLogger.LOG.patSkipped(annotatedType);
            return null;
        }

        ProcessAnnotatedTypeImpl<T> event = null;
        if (annotatedTypeContext.getExtension() == null) {
            event = new ProcessAnnotatedTypeImpl<T>(beanManager, annotatedType);
        } else {
            event = new ProcessSyntheticAnnotatedTypeImpl<T>(beanManager, annotatedTypeContext);
        }

        if (observers == null) {
            BootstrapLogger.LOG.patDefaultResolver(annotatedType);
            fireProcessAnnotatedType(event, beanManager);
        } else {
            BootstrapLogger.LOG.patFastResolver(annotatedType);
            fireProcessAnnotatedType(event, observers, beanManager);
        }
        return event;
    }

    /**
     * Fires a {@link ProcessAnnotatedType} or {@link ProcessSyntheticAnnotatedType} using the default event mechanism.
     */
    private void fireProcessAnnotatedType(ProcessAnnotatedTypeImpl<?> event, BeanManagerImpl beanManager) {
        final Resolvable resolvable = ProcessAnnotatedTypeEventResolvable.of(event, discovery);
        try {
            beanManager.getGlobalLenientObserverNotifier().fireEvent(event, resolvable);
        } catch (Exception e) {
            throw new DefinitionException(e);
        }
    }

    /**
     * Fires a {@link ProcessAnnotatedType}. Instead of using the default event dispatching mechanism, this method directly
     * notifies
     * extension observers resolved by FastProcessAnnotatedTypeResolver.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void fireProcessAnnotatedType(ProcessAnnotatedTypeImpl<?> event,
            Set<ContainerLifecycleEventObserverMethod<?>> observers,
            BeanManagerImpl beanManager) {
        List<Throwable> errors = new LinkedList<Throwable>();
        List<ContainerLifecycleEventObserverMethod<?>> sortedObserverMethods = new ArrayList<>(observers);
        sortedObserverMethods.sort(Comparator.comparingInt(ObserverMethod::getPriority));
        for (ContainerLifecycleEventObserverMethod observer : sortedObserverMethods) {
            // FastProcessAnnotatedTypeResolver does not consider special scope inheritance rules (see CDI - section 4.1)
            if (checkScopeInheritanceRules(event.getOriginalAnnotatedType(), observer, beanManager)) {
                try {
                    observer.notify(event);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new DefinitionException(errors);
        }
    }

    private boolean checkScopeInheritanceRules(SlimAnnotatedType<?> type, ContainerLifecycleEventObserverMethod<?> observer,
            BeanManagerImpl beanManager) {
        Collection<Class<? extends Annotation>> scopes;
        if (observer instanceof ExtensionObserverMethodImpl) {
            ExtensionObserverMethodImpl<?, ?> extensionObserver = (ExtensionObserverMethodImpl<?, ?>) observer;
            scopes = extensionObserver.getRequiredScopeAnnotations();
        } else {
            scopes = observer.getRequiredAnnotations().stream().filter((a) -> beanManager.isScope(a))
                    .collect(Collectors.toSet());
        }
        if (!scopes.isEmpty() && scopes.size() == observer.getRequiredAnnotations().size()) {
            // this check only works if only scope annotations are listed within @WithAnnotations
            // performing a complete check would be way too expensive - eliminating the benefit of ClassFileServices
            for (Class<? extends Annotation> annotation : scopes) {
                if (type.isAnnotationPresent(annotation)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void fireProcessBean(BeanManagerImpl beanManager, Bean<?> bean) {
        fireProcessBean(beanManager, bean, null);
    }

    public void fireProcessBean(BeanManagerImpl beanManager, Bean<?> bean, Extension extension) {
        if (isProcessBeanObserved()) {
            if (bean instanceof ManagedBean<?>) {
                ProcessManagedBeanImpl.fire(beanManager, (ManagedBean<?>) bean);
            } else if (bean instanceof SessionBean<?>) {
                ProcessSessionBeanImpl.fire(beanManager, Reflections.<SessionBean<Object>> cast(bean));
            } else if (bean instanceof ProducerField<?, ?>) {
                ProcessProducerFieldImpl.fire(beanManager, (ProducerField<?, ?>) bean);
            } else if (bean instanceof ProducerMethod<?, ?>) {
                ProcessProducerMethodImpl.fire(beanManager, (ProducerMethod<?, ?>) bean);
            } else {
                if (extension != null) {
                    ProcessSynthethicBeanImpl.fire(beanManager, bean, extension);
                } else {
                    ProcessBeanImpl.fire(beanManager, bean);
                }
            }
        }
    }

    public <T> ProcessBeanAttributesImpl<T> fireProcessBeanAttributes(BeanManagerImpl beanManager, BeanAttributes<T> attributes,
            Annotated annotated,
            Type type) {
        if (isProcessBeanAttributesObserved()) {
            return ProcessBeanAttributesImpl.fire(beanManager, attributes, annotated, type);
        }
        return null;
    }

    public void fireProcessInjectionTarget(BeanManagerImpl beanManager, AbstractClassBean<?> bean) {
        if (isProcessInjectionTargetObserved()) {
            AbstractProcessInjectionTarget.fire(beanManager, bean);
        }
    }

    public <X> InjectionTarget<X> fireProcessInjectionTarget(BeanManagerImpl beanManager, AnnotatedType<X> annotatedType,
            InjectionTarget<X> injectionTarget) {
        if (isProcessInjectionTargetObserved()) {
            return AbstractProcessInjectionTarget.fire(beanManager, annotatedType, injectionTarget);
        }
        return injectionTarget;
    }

    public <T, X> FieldInjectionPointAttributes<T, X> fireProcessInjectionPoint(FieldInjectionPointAttributes<T, X> attributes,
            Class<?> declaringComponentClass,
            BeanManagerImpl manager) {
        if (isProcessInjectionPointObserved()) {
            return ProcessInjectionPointImpl.fire(attributes, declaringComponentClass, manager);
        }
        return attributes;
    }

    public <T, X> ParameterInjectionPointAttributes<T, X> fireProcessInjectionPoint(
            ParameterInjectionPointAttributes<T, X> injectionPointAttributes,
            Class<?> declaringComponentClass, BeanManagerImpl manager) {
        if (isProcessInjectionPointObserved()) {
            return ProcessInjectionPointImpl.fire(injectionPointAttributes, declaringComponentClass, manager);
        }
        return injectionPointAttributes;
    }

    public <T, X> ObserverMethod<T> fireProcessObserverMethod(BeanManagerImpl beanManager, ObserverMethodImpl<T, X> observer) {
        return fireProcessObserverMethod(beanManager, observer.getMethod().getAnnotated(), observer, null);
    }

    public <T> ObserverMethod<T> fireProcessObserverMethod(BeanManagerImpl beanManager, ObserverMethod<T> observer,
            Extension extension) {
        return fireProcessObserverMethod(beanManager, null, observer, extension);
    }

    private <T, X> ObserverMethod<T> fireProcessObserverMethod(BeanManagerImpl beanManager, AnnotatedMethod<X> beanMethod,
            ObserverMethod<T> observerMethod,
            Extension extension) {
        if (isProcessObserverMethodObserved()) {
            if (extension != null) {
                return ProcessSyntheticObserverMethodImpl.fire(beanManager, beanMethod, observerMethod, extension);
            }
            return ProcessObserverMethodImpl.fire(beanManager, beanMethod, observerMethod);
        }
        return observerMethod;
    }

    public void fireProcessProducer(BeanManagerImpl beanManager, AbstractProducerBean<?, ?, Member> bean) {
        if (isProcessProducerObserved()) {
            ProcessProducerImpl.fire(beanManager, bean);
        }
    }

    public void preloadProcessAnnotatedType(Class<?> type) {
        if (preloader != null && isProcessAnnotatedTypeObserved()) {
            preloader.preloadContainerLifecycleEvent(ProcessAnnotatedType.class, type);
        }
    }

    public <T extends ProcessBean<?>> void preloadProcessBean(Class<T> eventRawType, Type... typeParameters) {
        if (preloader != null && isProcessBeanObserved()) {
            preloader.preloadContainerLifecycleEvent(ProcessAnnotatedType.class, typeParameters);
        }
    }

    public void preloadProcessBeanAttributes(Type type) {
        if (preloader != null && isProcessBeanAttributesObserved()) {
            preloader.preloadContainerLifecycleEvent(ProcessBeanAttributes.class, type);
        }
    }

    public void preloadProcessInjectionTarget(Class<?> type) {
        if (preloader != null && isProcessInjectionTargetObserved()) {
            preloader.preloadContainerLifecycleEvent(ProcessInjectionTarget.class, type);
        }
    }

    public void preloadProcessObserverMethod(Type... typeParameters) {
        if (preloader != null && isProcessObserverMethodObserved()) {
            preloader.preloadContainerLifecycleEvent(ProcessObserverMethod.class, typeParameters);
        }
    }

    public void preloadProcessProducer(Type... typeParameters) {
        if (preloader != null && isProcessProducerObserved()) {
            preloader.preloadContainerLifecycleEvent(ProcessProducer.class, typeParameters);
        }
    }

    @Override
    public void cleanupAfterBoot() {
        if (preloader != null) {
            preloader.shutdown();
        }
    }

    public boolean isPreloaderEnabled() {
        return preloader != null;
    }
}
