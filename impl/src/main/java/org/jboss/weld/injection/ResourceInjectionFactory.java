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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.builtin.ee.StaticEEResourceProducerField;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JaxwsInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.util.ApiAbstraction;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.ws.WSApiAbstraction;

/**
 * Factory class that produces {@link ResourceInjection} instances for resource fields and setter methods.
 *
 * @author Martin Kouba
 */
public final class ResourceInjectionFactory {

    private static final ResourceInjectionFactory INSTANCE = new ResourceInjectionFactory();

    private final List<ResourceInjectionProcessor<?, ?>> resourceInjectionProcessors;

    private ResourceInjectionFactory() {
        super();
        this.resourceInjectionProcessors = initializeProcessors();
    }

    /**
     * Returns the default {@link ResourceInjectionFactory} singleton.
     *
     * @return the default {@link ResourceInjectionFactory} singleton
     */
    public static ResourceInjectionFactory instance() {
        return INSTANCE;
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
        for (EnhancedAnnotatedType<?> actualType = type; actualType != null && !actualType.getJavaClass().equals(Object.class); actualType = actualType
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

    private List<ResourceInjectionProcessor<?, ?>> initializeProcessors() {
        List<ResourceInjectionProcessor<?, ?>> processors = new ArrayList<ResourceInjectionFactory.ResourceInjectionProcessor<?, ?>>();
        processors.add(new EjbResourceInjectionProcessor());
        processors.add(new PersistenceUnitResourceInjectionProcessor());
        processors.add(new PersistenceContextResourceInjectionProcessor());
        processors.add(new ResourceResourceInjectionProcessor());
        processors.add(new WebServiceResourceInjectionProcessor());
        return processors;
    }

    /**
     * Abstract stateless resource injection processor.
     *
     * @author Martin Kouba
     *
     * @param <S>
     * @param <A>
     */
    private abstract class ResourceInjectionProcessor<S extends Service, A extends ApiAbstraction> {

        /**
         * @param fieldInjectionPoint
         * @param beanManager
         * @return {@link ResourceInjection} for static producer field or <code>null</code> if required services are not
         *         supported or the field is not annotated with the specific marker annotation
         * @see StaticEEResourceProducerField
         */
        protected <T, X> ResourceInjection<T> createStaticProducerFieldResourceInjection(
                FieldInjectionPoint<T, X> fieldInjectionPoint, BeanManagerImpl beanManager) {

            S injectionServices = getInjectionServices(beanManager);
            A apiAbstraction = getApiAbstraction(beanManager);

            if (injectionServices != null && apiAbstraction != null
                    && fieldInjectionPoint.getAnnotated().isAnnotationPresent(getMarkerAnnotation(apiAbstraction))) {
                return createFieldResourceInjection(fieldInjectionPoint, injectionServices, apiAbstraction);
            }
            return null;
        }

        /**
         *
         * @param declaringBean
         * @param type
         * @param manager
         * @return the set of {@link ResourceInjection}s for the specified bean and type
         */
        protected Set<ResourceInjection<?>> createResourceInjections(Bean<?> declaringBean, EnhancedAnnotatedType<?> type,
                BeanManagerImpl manager) {

            S injectionServices = getInjectionServices(manager);
            A apiAbstraction = getApiAbstraction(manager);

            if (injectionServices == null || apiAbstraction == null) {
                return Collections.emptySet();
            }

            Class<? extends Annotation> marker = getMarkerAnnotation(apiAbstraction);
            ImmutableSet.Builder<ResourceInjection<?>> resourceInjections = ImmutableSet.builder();

            for (EnhancedAnnotatedField<?, ?> field : type.getDeclaredEnhancedFields(marker)) {
                resourceInjections.add(createFieldResourceInjection(InjectionPointFactory.silentInstance()
                        .createFieldInjectionPoint(field, declaringBean, type.getJavaClass(), manager), injectionServices,
                        apiAbstraction));
            }
            for (EnhancedAnnotatedMethod<?, ?> method : type.getDeclaredEnhancedMethods(marker)) {
                if (method.getParameters().size() != 1) {
                    throw UtilLogger.LOG.resourceSetterInjectionNotAJavabean(method);
                }
                resourceInjections.add(createSetterResourceInjection(
                        InjectionPointFactory.silentInstance().createParameterInjectionPoint(
                                method.getEnhancedParameters().get(0), declaringBean, type.getJavaClass(), manager),
                        injectionServices, apiAbstraction));
            }
            return resourceInjections.build();
        }

        /**
         *
         * @param fieldInjectionPoint
         * @param beanManager
         * @return {@link ResourceInjection} for the given field
         */
        private <T, X> ResourceInjection<T> createFieldResourceInjection(FieldInjectionPoint<T, X> fieldInjectionPoint,
                S injectionServices, A apiAbstraction) {
            return new FieldResourceInjection<T, X>(fieldInjectionPoint,
                    Reflections.<ResourceReferenceFactory<T>> cast(getResourceReferenceFactory(fieldInjectionPoint,
                            injectionServices, apiAbstraction)));
        }

        /**
         *
         * @param methodInjectionPoint
         * @param beanManager
         * @return {@link ResourceInjection} for the given setter method
         */
        private <T, X> ResourceInjection<T> createSetterResourceInjection(
                ParameterInjectionPoint<T, X> parameterInjectionPoint, S injectionServices, A apiAbstraction) {
            return new SetterResourceInjection<T, X>(parameterInjectionPoint,
                    Reflections.<ResourceReferenceFactory<T>> cast(getResourceReferenceFactory(parameterInjectionPoint,
                            injectionServices, apiAbstraction)));
        }

        protected abstract <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
                S injectionServices, A apiAbstraction);

        protected abstract Class<? extends Annotation> getMarkerAnnotation(A apiAbstraction);

        protected abstract A getApiAbstraction(BeanManagerImpl manager);

        protected abstract S getInjectionServices(BeanManagerImpl manager);

    }

    /**
     * EJB resource injection processor.
     */
    private class EjbResourceInjectionProcessor extends ResourceInjectionProcessor<EjbInjectionServices, EJBApiAbstraction> {

        @Override
        protected <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
                EjbInjectionServices injectionServices, EJBApiAbstraction apiAbstraction) {
            return Reflections.<ResourceReferenceFactory<T>> cast(injectionServices.registerEjbInjectionPoint(injectionPoint));
        }

        @Override
        protected Class<? extends Annotation> getMarkerAnnotation(EJBApiAbstraction apiAbstraction) {
            return apiAbstraction.EJB_ANNOTATION_CLASS;
        }

        @Override
        protected EJBApiAbstraction getApiAbstraction(BeanManagerImpl manager) {
            return manager.getServices().get(EJBApiAbstraction.class);
        }

        @Override
        protected EjbInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(EjbInjectionServices.class);
        }

    }

    /**
     * JPA persistence unit resource injection processor.
     *
     */
    private class PersistenceUnitResourceInjectionProcessor extends
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
        protected PersistenceApiAbstraction getApiAbstraction(BeanManagerImpl manager) {
            return manager.getServices().get(PersistenceApiAbstraction.class);
        }

        @Override
        protected JpaInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(JpaInjectionServices.class);
        }
    }

    /**
     * JPA persistence context resource injection processor.
     */
    private class PersistenceContextResourceInjectionProcessor extends
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
        protected PersistenceApiAbstraction getApiAbstraction(BeanManagerImpl manager) {
            return manager.getServices().get(PersistenceApiAbstraction.class);
        }

        @Override
        protected JpaInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(JpaInjectionServices.class);
        }

    }

    /**
     * Resource injection processor.
     */
    private class ResourceResourceInjectionProcessor extends
            ResourceInjectionProcessor<ResourceInjectionServices, EJBApiAbstraction> {

        @Override
        protected <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
                ResourceInjectionServices injectionServices, EJBApiAbstraction apiAbstraction) {
            return Reflections.<ResourceReferenceFactory<T>> cast(injectionServices
                    .registerResourceInjectionPoint(injectionPoint));
        }

        @Override
        protected Class<? extends Annotation> getMarkerAnnotation(EJBApiAbstraction apiAbstraction) {
            return Resource.class;
        }

        @Override
        protected EJBApiAbstraction getApiAbstraction(BeanManagerImpl manager) {
            return manager.getServices().get(EJBApiAbstraction.class);
        }

        @Override
        protected ResourceInjectionServices getInjectionServices(BeanManagerImpl manager) {
            return manager.getServices().get(ResourceInjectionServices.class);
        }

    }

    /**
     * JAX-WS resource injection processor.
     */
    private class WebServiceResourceInjectionProcessor extends
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
        protected WSApiAbstraction getApiAbstraction(BeanManagerImpl manager) {
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

}
