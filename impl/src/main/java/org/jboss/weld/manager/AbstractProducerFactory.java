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
package org.jboss.weld.manager;

import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.enterprise.inject.spi.ProducerFactory;

import org.jboss.weld.annotated.AnnotatedTypeValidator;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.logging.BeanManagerLogger;

public abstract class AbstractProducerFactory<X> implements ProducerFactory<X> {

    private final Bean<X> declaringBean;
    private final BeanManagerImpl manager;

    protected AbstractProducerFactory(Bean<X> declaringBean, BeanManagerImpl manager) {
        this.declaringBean = declaringBean;
        this.manager = manager;
    }

    protected Bean<X> getDeclaringBean() {
        return declaringBean;
    }

    protected BeanManagerImpl getManager() {
        return manager;
    }

    protected abstract AnnotatedMember<X> getAnnotatedMember();

    public abstract <T> Producer<T> createProducer(final Bean<X> declaringBean, final Bean<T> bean,
            DisposalMethod<X, T> disposalMethod);

    @Override
    public <T> Producer<T> createProducer(Bean<T> bean) {
        if (getDeclaringBean() == null && !getAnnotatedMember().isStatic()) {
            throw BeanManagerLogger.LOG.nullDeclaringBean(getAnnotatedMember());
        }
        AnnotatedTypeValidator.validateAnnotatedMember(getAnnotatedMember());
        try {
            Producer<T> producer = createProducer(getDeclaringBean(), bean, null);
            getManager().getServices().get(InjectionTargetService.class).validateProducer(producer);
            return producer;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }
}
