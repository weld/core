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
package org.jboss.weld.tests.security.members;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.Producer;

public class SimpleExtension implements Extension {

    private AnnotatedType<SimpleBean> simpleBeanType1;
    private AnnotatedType<SimpleBean> simpleBeanType2;
    private AnnotatedType<SimpleBean> simpleBeanType3;
    private AnnotatedType<SimpleBean> simpleBeanType4;
    private AnnotatedType<SimpleDecorator> simpleDecoratorType1;
    private AnnotatedType<SimpleDecorator> simpleDecoratorType2;
    private AnnotatedType<SimpleDecorator> simpleDecoratorType3;

    private AnnotatedMember<?> integerMethod1;
    private AnnotatedMember<?> integerMethod2;

    private AnnotatedMember<?> producerField;

    private AnnotatedParameter<?> disposerParameter1;
    private AnnotatedParameter<?> disposerParameter2;

    private InjectionTarget<SimpleBean> simpleBeanInjectionTarget;
    private Producer<Integer> integerBeanProducer;

    private Bean<?> simpleBean;
    private Bean<?> simpleDecorator;
    private Bean<?> integerBean;
    private Bean<?> producerFieldBean;

    private AnnotatedMethod<?> observerMethodMember;

    void observeSimpleBeanType1(@Observes ProcessAnnotatedType<SimpleBean> event) {
        this.simpleBeanType1 = event.getAnnotatedType();
    }

    void observeSimpleDecoratorType1(@Observes ProcessAnnotatedType<SimpleDecorator> event) {
        this.simpleDecoratorType1 = event.getAnnotatedType();
    }

    void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        this.simpleBeanType2 = event.getAnnotatedType(SimpleBean.class, null);
        this.simpleDecoratorType2 = event.getAnnotatedType(SimpleDecorator.class, null);
    }

    void observeSimpleBeanInjectionTarget(@Observes ProcessInjectionTarget<SimpleBean> event) {
        this.simpleBeanInjectionTarget = event.getInjectionTarget();
        this.simpleBeanType3 = event.getAnnotatedType();
    }

    void observeIntegerBeanProducer(@Observes ProcessProducer<SimpleBean, Integer> event) {
        this.integerBeanProducer = event.getProducer();
        this.integerMethod1 = event.getAnnotatedMember();
    }

    @SuppressWarnings("unchecked")
    void observeSimpleBean(@Observes ProcessManagedBean<SimpleBean> event) {
        this.simpleBean = event.getBean();
        this.simpleBeanType4 = (AnnotatedType<SimpleBean>) event.getAnnotated();
    }

    @SuppressWarnings("unchecked")
    void observeSimpleDecorator(@Observes ProcessManagedBean<SimpleDecorator> event) {
        this.simpleDecorator = event.getBean();
        this.simpleDecoratorType3 = (AnnotatedType<SimpleDecorator>) event.getAnnotated();
    }

    void observeProducerMethod(@Observes ProcessProducerMethod<Integer, SimpleBean> event) {
        this.integerBean = event.getBean();
        this.integerMethod2 = event.getAnnotatedProducerMethod();
        this.disposerParameter1 = event.getAnnotatedDisposedParameter();
    }

    void observeProducerField(@Observes ProcessProducerField<Float, SimpleBean> event) {
        this.producerFieldBean = event.getBean();
        this.producerField = event.getAnnotatedProducerField();
        this.disposerParameter2 = event.getAnnotatedDisposedParameter();
    }

    void observeObserverMethod(@Observes ProcessObserverMethod<String, SimpleBean> event) {
        this.observerMethodMember = event.getAnnotatedMethod();
    }

    AnnotatedMethod<?> getObserverMethodMember() {
        return observerMethodMember;
    }

    AnnotatedMember<?> getProducerField() {
        return producerField;
    }

    AnnotatedParameter<?> getDisposerParameter1() {
        return disposerParameter1;
    }

    AnnotatedParameter<?> getDisposerParameter2() {
        return disposerParameter2;
    }

    Bean<?> getProducerFieldBean() {
        return producerFieldBean;
    }

    AnnotatedMember<?> getIntegerMethod2() {
        return integerMethod2;
    }

    AnnotatedType<SimpleBean> getSimpleBeanType1() {
        return simpleBeanType1;
    }

    AnnotatedType<SimpleBean> getSimpleBeanType2() {
        return simpleBeanType2;
    }

    AnnotatedType<SimpleBean> getSimpleBeanType3() {
        return simpleBeanType3;
    }

    AnnotatedMember<?> getIntegerMethod1() {
        return integerMethod1;
    }

    InjectionTarget<SimpleBean> getSimpleBeanInjectionTarget() {
        return simpleBeanInjectionTarget;
    }

    Producer<Integer> getIntegerBeanProducer() {
        return integerBeanProducer;
    }

    AnnotatedType<SimpleBean> getSimpleBeanType4() {
        return simpleBeanType4;
    }

    Bean<?> getSimpleBean() {
        return simpleBean;
    }

    Bean<?> getIntegerBean() {
        return integerBean;
    }

    AnnotatedType<SimpleDecorator> getSimpleDecoratorType1() {
        return simpleDecoratorType1;
    }

    AnnotatedType<SimpleDecorator> getSimpleDecoratorType2() {
        return simpleDecoratorType2;
    }

    AnnotatedType<SimpleDecorator> getSimpleDecoratorType3() {
        return simpleDecoratorType3;
    }

    Bean<?> getSimpleDecorator() {
        return simpleDecorator;
    }

}
