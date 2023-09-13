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

import static org.jboss.weld.util.reflection.Reflections.cast;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Producer;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.injection.producer.ProducerFieldProducer;
import org.jboss.weld.resources.MemberTransformer;

public class FieldProducerFactory<X> extends AbstractProducerFactory<X> {

    private final AnnotatedField<X> field;

    protected FieldProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean, BeanManagerImpl manager) {
        super(declaringBean, manager);
        this.field = cast(field);
    }

    /**
     * Producers returned from this method are not validated. Internal use only.
     */
    @Override
    public <T> Producer<T> createProducer(final Bean<X> declaringBean, final Bean<T> bean,
            DisposalMethod<X, T> disposalMethod) {
        EnhancedAnnotatedField<T, X> enhancedField = getManager().getServices().get(MemberTransformer.class)
                .loadEnhancedMember(field, getManager().getId());
        return new ProducerFieldProducer<X, T>(enhancedField, disposalMethod) {

            @Override
            public AnnotatedField<X> getAnnotated() {
                return field;
            }

            @Override
            public BeanManagerImpl getBeanManager() {
                return getManager();
            }

            @Override
            public Bean<X> getDeclaringBean() {
                return declaringBean;
            }

            @Override
            public Bean<T> getBean() {
                return bean;
            }
        };
    }

    @Override
    protected AnnotatedMember<X> getAnnotatedMember() {
        return field;
    }

}
