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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.Collection;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.annotated.AnnotatedTypeValidator;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

public class AbstractAnnotatedTypeRegisteringEvent extends AbstractBeanDiscoveryEvent {

    /*
     * The receiver object and the observer method being used for event dispatch at a given time. This information is required
     * for implementing ProcessSyntheticAnnotatedType properly. The information must be set by an
     * ObserverMethod implementation before invoking the target observer method and unset once the notification is complete.
     */
    private Object receiver;

    protected AbstractAnnotatedTypeRegisteringEvent(BeanManagerImpl beanManager, Type rawType, BeanDeploymentArchiveMapping bdaMapping, Deployment deployment, Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, rawType, bdaMapping, deployment, contexts);
    }

    protected void addSyntheticAnnotatedType(AnnotatedType<?> type, String id) {
        AnnotatedTypeValidator.validateAnnotatedType(type);
        if (Beans.isVetoed(type)) {
            return;
        }
        storeSyntheticAnnotatedType(getOrCreateBeanDeployment(type.getJavaClass()), type, id);
    }

    protected Extension getSyntheticAnnotatedTypeSource() {
        Object receiver = getReceiver();
        if (!(receiver instanceof Extension)) {
            throw new IllegalStateException("Container lifecycle event observer is not an extension");
        }
        return (Extension) receiver;
    }

    protected void storeSyntheticAnnotatedType(BeanDeployment deployment, AnnotatedType<?> type, String id) {
        deployment.getBeanDeployer().addSyntheticClass(type, getSyntheticAnnotatedTypeSource(), id);
    }

    public Object getReceiver() {
        return receiver;
    }

    public void setReceiver(Object receiver) {
        this.receiver = receiver;
    }

}
