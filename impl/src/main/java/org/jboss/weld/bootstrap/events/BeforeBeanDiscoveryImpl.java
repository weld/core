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
package org.jboss.weld.bootstrap.events;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.events.configurator.AnnotatedTypeConfiguratorImpl;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.literal.InterceptorBindingTypeLiteral;
import org.jboss.weld.literal.InvokableLiteral;
import org.jboss.weld.literal.NormalScopeLiteral;
import org.jboss.weld.literal.QualifierLiteral;
import org.jboss.weld.literal.ScopeLiteral;
import org.jboss.weld.literal.StereotypeLiteral;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.util.annotated.AnnotatedTypeWrapper;

public class BeforeBeanDiscoveryImpl extends AbstractAnnotatedTypeRegisteringEvent implements BeforeBeanDiscovery {

    protected final List<AnnotatedTypeConfiguratorImpl<? extends Annotation>> additionalQualifiers;
    protected final List<AnnotatedTypeConfiguratorImpl<? extends Annotation>> additionalInterceptorBindings;

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping,
            Collection<ContextHolder<? extends Context>> contexts) {
        BeforeBeanDiscoveryImpl event = new BeforeBeanDiscoveryImpl(beanManager, deployment, bdaMapping, contexts);
        event.fire();
        event.finish();
    }

    protected BeforeBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment,
            BeanDeploymentArchiveMapping bdaMapping, Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, BeforeBeanDiscovery.class, bdaMapping, deployment, contexts);
        additionalQualifiers = new LinkedList<>();
        additionalInterceptorBindings = new LinkedList<>();
    }

    @Override
    public void addQualifier(Class<? extends Annotation> bindingType) {
        checkWithinObserverNotification();
        getTypeStore().add(bindingType, QualifierLiteral.INSTANCE);
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(bindingType);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(bindingType);
        BootstrapLogger.LOG.addQualifierCalled(getReceiver(), bindingType);
    }

    @Override
    public void addInterceptorBinding(Class<? extends Annotation> bindingType, Annotation... bindingTypeDef) {
        checkWithinObserverNotification();
        TypeStore typeStore = getTypeStore();
        typeStore.add(bindingType, InterceptorBindingTypeLiteral.INSTANCE);
        for (Annotation a : bindingTypeDef) {
            typeStore.add(bindingType, a);
        }
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(bindingType);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(bindingType);
        BootstrapLogger.LOG.addInterceptorBindingCalled(getReceiver(), bindingType);
    }

    @Override
    public void addInvokable(Class<? extends Annotation> aClass) {
        checkWithinObserverNotification();
        getTypeStore().add(aClass, InvokableLiteral.INSTANCE);
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(aClass);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(aClass);
        BootstrapLogger.LOG.addInvokableCalled(getReceiver(), aClass);
    }

    @Override
    public void addScope(Class<? extends Annotation> scopeType, boolean normal, boolean passivating) {
        checkWithinObserverNotification();
        if (normal) {
            getTypeStore().add(scopeType, new NormalScopeLiteral(passivating));
        } else if (passivating) {
            throw BootstrapLogger.LOG.passivatingNonNormalScopeIllegal(scopeType);
        } else {
            getTypeStore().add(scopeType, ScopeLiteral.INSTANCE);
        }
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(scopeType);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(scopeType);
        getBeanManager().getServices().get(ReflectionCache.class).cleanup();
        BootstrapLogger.LOG.addScopeCalled(getReceiver(), scopeType);
    }

    @Override
    public void addStereotype(Class<? extends Annotation> stereotype, Annotation... stereotypeDef) {
        checkWithinObserverNotification();
        TypeStore typeStore = getTypeStore();
        typeStore.add(stereotype, StereotypeLiteral.INSTANCE);
        for (Annotation a : stereotypeDef) {
            typeStore.add(stereotype, a);
        }
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(stereotype);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(stereotype);
        BootstrapLogger.LOG.addStereoTypeCalled(getReceiver(), stereotype);
    }

    @Override
    public void addAnnotatedType(AnnotatedType<?> type, String id) {
        checkWithinObserverNotification();
        addSyntheticAnnotatedType(type, id);
        BootstrapLogger.LOG.addAnnotatedTypeCalledInBBD(getReceiver(), type);
    }

    @Override
    public <T> AnnotatedTypeConfigurator<T> addAnnotatedType(Class<T> type, String id) {
        checkWithinObserverNotification();
        AnnotatedTypeConfiguratorImpl<T> configurator = new AnnotatedTypeConfiguratorImpl<>(
                getBeanManager().createAnnotatedType(type));
        additionalAnnotatedTypes.add(new AnnotatedTypeRegistration<T>(configurator, id));
        BootstrapLogger.LOG.addAnnotatedTypeCalledInBBD(getReceiver(), type);
        return configurator;
    }

    @Override
    public void addQualifier(AnnotatedType<? extends Annotation> qualifier) {
        checkWithinObserverNotification();
        addSyntheticAnnotation(qualifier, QualifierLiteral.INSTANCE);
        BootstrapLogger.LOG.addQualifierCalled(getReceiver(), qualifier);
    }

    @Override
    public void addInterceptorBinding(AnnotatedType<? extends Annotation> bindingType) {
        checkWithinObserverNotification();
        addSyntheticAnnotation(bindingType, InterceptorBindingTypeLiteral.INSTANCE);
        BootstrapLogger.LOG.addInterceptorBindingCalled(getReceiver(), bindingType);
    }

    @Override
    public <T extends Annotation> AnnotatedTypeConfigurator<T> configureQualifier(Class<T> qualifier) {
        checkWithinObserverNotification();
        AnnotatedTypeConfiguratorImpl<T> configurator = new AnnotatedTypeConfiguratorImpl<>(
                getBeanManager().createAnnotatedType(qualifier));
        additionalQualifiers.add(configurator);
        BootstrapLogger.LOG.configureQualifierCalled(getReceiver(), qualifier);
        return configurator;
    }

    @Override
    public <T extends Annotation> AnnotatedTypeConfigurator<T> configureInterceptorBinding(Class<T> bindingType) {
        checkWithinObserverNotification();
        AnnotatedTypeConfiguratorImpl<T> configurator = new AnnotatedTypeConfiguratorImpl<>(
                getBeanManager().createAnnotatedType(bindingType));
        additionalInterceptorBindings.add(configurator);
        BootstrapLogger.LOG.configureInterceptorBindingCalled(getReceiver(), bindingType);
        return configurator;
    }

    @Override
    protected void finish() {
        super.finish();
        try {
            for (AnnotatedTypeConfiguratorImpl<? extends Annotation> qualifierAsAnnotatedType : additionalQualifiers) {
                addSyntheticAnnotation(qualifierAsAnnotatedType.complete(), QualifierLiteral.INSTANCE);
            }
            for (AnnotatedTypeConfiguratorImpl<? extends Annotation> interceptorBindingAsAnnotatedType : additionalInterceptorBindings) {
                addSyntheticAnnotation(interceptorBindingAsAnnotatedType.complete(), InterceptorBindingTypeLiteral.INSTANCE);
            }
        } catch (Exception e) {
            throw new DefinitionException(e);
        }
    }

    private <A extends Annotation> void addSyntheticAnnotation(AnnotatedType<A> annotation, Annotation requiredMetaAnnotation) {
        if (requiredMetaAnnotation != null && !annotation.isAnnotationPresent(requiredMetaAnnotation.annotationType())) {
            // Add required meta annotation
            annotation = new AnnotatedTypeWrapper<A>(annotation, requiredMetaAnnotation);
        }
        getBeanManager().getServices().get(ClassTransformer.class).addSyntheticAnnotation(annotation, getBeanManager().getId());
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(annotation.getJavaClass());
    }

}
