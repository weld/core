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
package org.jboss.weld.bean.builtin;

import static org.jboss.weld.logging.messages.BeanMessage.DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.ejb.SessionBeanInjectionPoint;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.bean.SerializableForwardingInjectionPoint;

/**
 * Bean for InjectionPoint metadata
 *
 * @author David Allen
 */
public class InjectionPointBean extends AbstractStaticallyDecorableBuiltInBean<InjectionPoint> {

    private final CurrentInjectionPoint currentInjectionPointService;

    /**
     * Creates an InjectionPoint Web Bean for the injection of the containing bean owning
     * the field, constructor or method for the annotated item
     *
     * @param <T>     must be InjectionPoint
     * @param <S>
     * @param field   The annotated member field/parameter for the injection
     * @param manager The RI manager implementation
     */
    public InjectionPointBean(BeanManagerImpl manager) {
        super(manager, InjectionPoint.class);
        this.currentInjectionPointService = getBeanManager().getServices().get(CurrentInjectionPoint.class);
    }

    protected InjectionPoint newInstance(InjectionPoint ip, CreationalContext<InjectionPoint> creationalContext) {
        InjectionPoint injectionPoint = currentInjectionPointService.peek();
        if (injectionPoint instanceof SerializableForwardingInjectionPoint) {
            return injectionPoint;
        }
        injectionPoint = new SerializableForwardingInjectionPoint(getBeanManager().getContextId(), injectionPoint);
        injectionPoint = SessionBeanInjectionPoint.wrapIfNecessary(injectionPoint);
        return injectionPoint;
    }

    public void destroy(InjectionPoint instance, CreationalContext<InjectionPoint> creationalContext) {

    }


    @Override
    protected InjectionPoint getInjectionPoint(CurrentInjectionPoint cip) {
        InjectionPoint ip = super.getInjectionPoint(cip);
        if (ip == null) {
            throw new IllegalArgumentException(DYNAMIC_LOOKUP_OF_BUILT_IN_NOT_ALLOWED, toString());
        }
        return ip;
    }

    @Override
    public String toString() {
        return "Implicit Bean [javax.enterprise.inject.spi.InjectionPoint] with qualifiers [@Default]";
    }

}
