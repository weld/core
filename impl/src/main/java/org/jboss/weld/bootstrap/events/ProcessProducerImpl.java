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

import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import java.lang.reflect.Member;
import java.lang.reflect.Type;


public class ProcessProducerImpl<T, X> extends AbstractDefinitionContainerEvent implements ProcessProducer<T, X> {

    protected static <T, X> void fire(BeanManagerImpl beanManager, AbstractProducerBean<T, X, Member> bean) {
        if (beanManager.isBeanEnabled(bean)) {
            new ProcessProducerImpl<T, X>(beanManager, Reflections.<AnnotatedMember<T>>cast(bean.getAnnotated()), bean) {
            }.fire();
        }
    }

    private final AnnotatedMember<T> annotatedMember;
    private AbstractProducerBean<T, X, ?> bean;

    private ProcessProducerImpl(BeanManagerImpl beanManager, AnnotatedMember<T> annotatedMember, AbstractProducerBean<T, X, ?> bean) {
        super(beanManager, ProcessProducer.class, new Type[]{bean.getAnnotated().getDeclaringType().getBaseType(), bean.getAnnotated().getBaseType()});
        this.bean = bean;
        this.annotatedMember = annotatedMember;
    }

    public void addDefinitionError(Throwable t) {
        getErrors().add(t);
    }

    public AnnotatedMember<T> getAnnotatedMember() {
        return annotatedMember;
    }

    public Producer<X> getProducer() {
        return bean.getProducer();
    }

    public void setProducer(Producer<X> producer) {
        this.bean.setProducer(producer);
    }

}
