/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.ejb;

import java.lang.annotation.Annotation;

import javax.ejb.EJB;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.ResourceInjectionProcessor;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * EJB resource injection processor.
 */
class EjbResourceInjectionProcessor extends ResourceInjectionProcessor<EjbInjectionServices, Object> {

    @Override
    protected <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
            EjbInjectionServices injectionServices, Object processorContext) {
        return Reflections.<ResourceReferenceFactory<T>> cast(injectionServices.registerEjbInjectionPoint(injectionPoint));
    }

    @Override
    protected Class<? extends Annotation> getMarkerAnnotation(Object processorContext) {
        return EJB.class;
    }

    @Override
    protected Object getProcessorContext(BeanManagerImpl manager) {
        return null;
    }

    @Override
    protected EjbInjectionServices getInjectionServices(BeanManagerImpl manager) {
        return manager.getServices().get(EjbInjectionServices.class);
    }

}
