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
package org.jboss.weld.tests.injectionPoint.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;

import org.jboss.weld.literal.DefaultLiteral;

public class BarBean implements Bean<Bar> {

    private BeanManager beanManager;
    public static CustomInjectionPoint customInjectionPoint = null;
    public static InjectionPoint obtainedAsBean;
    public static InjectionPoint obtainedAsInjectableReference;
    private AnnotatedType<Bar> annotatedType;


    public BarBean(BeanManager beanManager, AnnotatedField<? super Bar> annotatedField, AnnotatedType<Bar> annotatedType) {
        this.beanManager = beanManager;
        this.annotatedType = annotatedType;
        customInjectionPoint = new CustomInjectionPoint(this, annotatedField, InjectionPoint.class);
    }

    @Override
    public Bar create(CreationalContext<Bar> creationalContext) {
        Bean<Bar> bean = (Bean<Bar>) beanManager.resolve(beanManager.getBeans(Bar.class));
        InjectionTargetFactory<Bar> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        InjectionTarget<Bar> injectionTarget = injectionTargetFactory.createInjectionTarget(bean);
        Bar bar = new Bar();
        injectionTarget.inject(bar, creationalContext);

        Bean<InjectionPoint> ipBean = (Bean<InjectionPoint>) beanManager.resolve(beanManager.getBeans(InjectionPoint.class));
        obtainedAsBean = (InjectionPoint) beanManager.getReference(ipBean, InjectionPoint.class, beanManager.createCreationalContext(ipBean));
        obtainedAsInjectableReference = (InjectionPoint) beanManager.getInjectableReference(customInjectionPoint, creationalContext);

        return bar;
    }

    @Override
    public void destroy(Bar instance, CreationalContext<Bar> creationalContext) {
        creationalContext.release();
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> immutableSet(T... items) {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, items);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Type> getTypes() {
        return this.<Type>immutableSet(Object.class, Bar.class);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return immutableSet(DefaultLiteral.INSTANCE);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Class<?> getBeanClass() {
        return Bar.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
       return Collections.<InjectionPoint>singleton(customInjectionPoint);
    }

}
