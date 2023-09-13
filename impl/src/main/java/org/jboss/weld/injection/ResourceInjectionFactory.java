/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection;

import static org.jboss.weld.util.reflection.Reflections.getRawType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.injection.spi.JaxwsInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.ws.WSApiAbstraction;

/**
 * Factory class that produces {@link ResourceInjection} instances for resource fields and setter methods.
 *
 * @author Martin Kouba
 */
public final class ResourceInjectionFactory implements Service, Iterable<ResourceInjectionProcessor<?, ?>> {

    private final List<ResourceInjectionProcessor<?, ?>> resourceInjectionProcessors;

    public ResourceInjectionFactory() {
        this.resourceInjectionProcessors = new CopyOnWriteArrayList<>();
        initializeProcessors();
    }

    public void addResourceInjectionProcessor(ResourceInjectionProcessor<?, ?> processor) {
        resourceInjectionProcessors.add(processor);
    }

    /**
     *
     * @param declaringBean
     * @param type
     * @param manager
     * @return resource injections for the given bean
     */
    public List<Set<ResourceInjection<?>>> getResourceInjections(Bean<?> declaringBean, EnhancedAnnotatedType<?> type,
            BeanManagerImpl manager) {

        List<Set<ResourceInjection<?>>> result = new ArrayList<Set<ResourceInjection<?>>>();

        // Iterate through the type hierarchy
        for (EnhancedAnnotatedType<?> actualType = type; actualType != null
                && !actualType.getJavaClass().equals(Object.class); actualType = actualType
                        .getEnhancedSuperclass()) {

            Set<ResourceInjection<?>> resourceInjections = discoverType(declaringBean, actualType, manager);
            if (!resourceInjections.isEmpty()) {
                result.add(resourceInjections);
            }
        }
        Collections.reverse(result);
        return result;
    }

    /**
     *
     *
     * @param fieldInjectionPoint
     * @param beanManager
     * @return the corresponding resource injection for the given field injection point, or <code>null</code> if no such
     *         resource injection exists
     */
    public <T, X> ResourceInjection<T> getStaticProducerFieldResourceInjection(FieldInjectionPoint<T, X> fieldInjectionPoint,
            BeanManagerImpl beanManager) {
        ResourceInjection<T> resourceInjection = null;
        for (ResourceInjectionProcessor<?, ?> processor : resourceInjectionProcessors) {
            resourceInjection = processor.createStaticProducerFieldResourceInjection(fieldInjectionPoint, beanManager);
            if (resourceInjection != null) {
                break;
            }
        }
        return resourceInjection;
    }

    private void initializeProcessors() {
        resourceInjectionProcessors.add(new PersistenceUnitResourceInjectionProcessor());
        resourceInjectionProcessors.add(new PersistenceContextResourceInjectionProcessor());
        resourceInjectionProcessors.add(new ResourceResourceInjectionProcessor());
        resourceInjectionProcessors.add(new WebServiceResourceInjectionProcessor());
    }

    /**
     * JPA persistence unit resource injection processor.
     *
     */
    private static class PersistenceUnitResourceInjectionProcessor extends
            ResourceInjectionProcessor<JpaInjectionServices, PersistenceApiAbstraction> {

        @Override
        protected <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
                JpaInjectionServices injectionServices, PersistenceApiAbstraction apiAbstraction) {

            if (!injectionPoint.getType().equals(apiAbstraction.ENTITY_MANAGER_FACTORY_CLASS)) {
                throw BeanLogger.LOG.invalidResourceProducerType(injectionPoint.getAnnotated(),
                        apiAbstraction.ENTITY_MANAGER_FACTORY_CLASS);
            }
            return Reflections.<ResourceReferenceFactory<T>> cast(injectionServices
                    .registerPersistenceUnitInjectionPoint(injectionPoint));
        }

        @Override
        protected Class<? extends Annotation> getMarkerAnnotation(PersistenceApiAbstraction apiAbstraction) {
            return apiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS;
        }

        @Override
        protected PersistenceApiAbstraction getProcessorContext(BeanManagerImpl manager) {
            return manager.getServices().get(PersistenceApiAbstraction.class);
        }

        @Override
        protected JpaInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(JpaInjectionServices.class);
        }

        @Override
        protected boolean accept(AnnotatedMember<?> member, PersistenceApiAbstraction apiAbstraction) {
            // ugly hack that allows application servers to support hibernate session injection while at the same time
            // the injection points are validated by Weld for invalid types (required by the TCK)
            return !apiAbstraction.SESSION_FACTORY_NAME.equals(getRawType(getResourceInjectionPointType(member)).getName());
        }
    }

    /**
     * JPA persistence context resource injection processor.
     */
    private static class PersistenceContextResourceInjectionProcessor extends
            ResourceInjectionProcessor<JpaInjectionServices, PersistenceApiAbstraction> {

        @Override
        protected <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
                JpaInjectionServices injectionServices, PersistenceApiAbstraction apiAbstraction) {

            if (!injectionPoint.getType().equals(apiAbstraction.ENTITY_MANAGER_CLASS)) {
                throw BeanLogger.LOG.invalidResourceProducerType(injectionPoint.getAnnotated(),
                        apiAbstraction.ENTITY_MANAGER_CLASS);
            }
            return Reflections.<ResourceReferenceFactory<T>> cast(injectionServices
                    .registerPersistenceContextInjectionPoint(injectionPoint));
        }

        @Override
        protected Class<? extends Annotation> getMarkerAnnotation(PersistenceApiAbstraction apiAbstraction) {
            return apiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
        }

        @Override
        protected PersistenceApiAbstraction getProcessorContext(BeanManagerImpl manager) {
            return manager.getServices().get(PersistenceApiAbstraction.class);
        }

        @Override
        protected JpaInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(JpaInjectionServices.class);
        }

        @Override
        protected boolean accept(AnnotatedMember<?> member, PersistenceApiAbstraction apiAbstraction) {
            // ugly hack that allows application servers to support hibernate session injection while at the same time
            // the injection points are validated by Weld for invalid types (required by the TCK)
            return !apiAbstraction.SESSION_NAME.equals(getRawType(getResourceInjectionPointType(member)).getName());
        }
    }

    /**
     * Resource injection processor.
     */
    private static class ResourceResourceInjectionProcessor extends
            ResourceInjectionProcessor<ResourceInjectionServices, Object> {

        @Override
        protected <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
                ResourceInjectionServices injectionServices, Object processorContext) {
            return Reflections.<ResourceReferenceFactory<T>> cast(injectionServices
                    .registerResourceInjectionPoint(injectionPoint));
        }

        @Override
        protected Class<? extends Annotation> getMarkerAnnotation(Object processorContext) {
            return Resource.class;
        }

        @Override
        protected Object getProcessorContext(BeanManagerImpl manager) {
            return null;
        }

        @Override
        protected ResourceInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(ResourceInjectionServices.class);
        }

    }

    /**
     * JAX-WS resource injection processor.
     */
    private static class WebServiceResourceInjectionProcessor extends
            ResourceInjectionProcessor<JaxwsInjectionServices, WSApiAbstraction> {

        @Override
        protected <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
                JaxwsInjectionServices injectionServices, WSApiAbstraction apiAbstraction) {
            return Reflections.<ResourceReferenceFactory<T>> cast(injectionServices
                    .registerWebServiceRefInjectionPoint(injectionPoint));
        }

        @Override
        protected Class<? extends Annotation> getMarkerAnnotation(WSApiAbstraction apiAbstraction) {
            return apiAbstraction.WEB_SERVICE_REF_ANNOTATION_CLASS;
        }

        @Override
        protected WSApiAbstraction getProcessorContext(BeanManagerImpl manager) {
            return manager.getServices().get(WSApiAbstraction.class);
        }

        @Override
        protected JaxwsInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(JaxwsInjectionServices.class);
        }

    }

    /**
     *
     * @param bean
     * @param type
     * @param manager
     * @return found resource injections for the given bean and type
     */
    private Set<ResourceInjection<?>> discoverType(Bean<?> bean, EnhancedAnnotatedType<?> type, BeanManagerImpl manager) {

        Set<ResourceInjection<?>> resourceInjections = new HashSet<ResourceInjection<?>>();

        for (ResourceInjectionProcessor<?, ?> processor : resourceInjectionProcessors) {
            resourceInjections.addAll(processor.createResourceInjections(bean, type, manager));
        }
        return resourceInjections;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public Iterator<ResourceInjectionProcessor<?, ?>> iterator() {
        return resourceInjectionProcessors.iterator();
    }

}
