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

import java.lang.reflect.Type;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;

public class BarExtension implements Extension {

    void observersABD(@Observes AfterBeanDiscovery event, BeanManager bm) {
        AnnotatedType<Bar> annotatedType = bm.createAnnotatedType(Bar.class);
        event.addBean(new BarBean(bm, getAnnotatedField(annotatedType, InjectionPoint.class), annotatedType));

    }

    private AnnotatedField<? super Bar> getAnnotatedField(AnnotatedType<Bar> annotatedType, Type memberType) {

        for (AnnotatedField<? super Bar> field : annotatedType.getFields()) {
            if (field.getJavaMember().getType().equals(memberType)) {
                return field;
            }
        }
        return null;
    }
}
