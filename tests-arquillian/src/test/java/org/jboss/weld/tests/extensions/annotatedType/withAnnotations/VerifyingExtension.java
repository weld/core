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
package org.jboss.weld.tests.extensions.annotatedType.withAnnotations;

import static org.junit.Assert.assertNull;

import java.beans.ConstructorProperties;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.inject.Named;
import jakarta.validation.Constraint;

public class VerifyingExtension implements Extension {

    private AnnotatedType<Person> personType;

    private AnnotatedType<Group> groupType;

    private AnnotatedType<MyBean> myBeanType;

    private AnnotatedType<MyBeanMeta> myBeanMetaType;

    void processPerson(@Observes @WithAnnotations(Constraint.class) ProcessAnnotatedType<Person> event) {
        assertNull(personType);
        this.personType = event.getAnnotatedType();
    }

    void processGroup(@Observes @WithAnnotations(ConstructorProperties.class) ProcessAnnotatedType<Group> event) {
        assertNull(groupType);
        this.groupType = event.getAnnotatedType();
    }

    void processMyBean(@Observes @WithAnnotations(Named.class) ProcessAnnotatedType<MyBean> event) {
        assertNull(myBeanType);
        this.myBeanType = event.getAnnotatedType();
    }

    void processMyBeanMeta(@Observes @WithAnnotations(Stereotype.class) ProcessAnnotatedType<MyBeanMeta> event) {
        assertNull(myBeanMetaType);
        this.myBeanMetaType = event.getAnnotatedType();
    }

    AnnotatedType<Person> getPersonType() {
        return personType;
    }

    AnnotatedType<Group> getGroupType() {
        return groupType;
    }

    AnnotatedType<MyBean> getMyBeanType() {
        return myBeanType;
    }

    AnnotatedType<MyBeanMeta> getMyBeanMetaType() {
        return myBeanMetaType;
    }

}
