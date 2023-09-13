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
package org.jboss.weld.tests.specialization.qualifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 * @see WELD-2046
 */
@RunWith(Arquillian.class)
public class SpecializingBeanQualifiersTest {

    @Inject
    BeanManager beanManager;

    @Deployment
    public static Archive<?> createArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SpecializingBeanQualifiersTest.class))
                .addPackage(
                        SpecializingBeanQualifiersTest.class.getPackage());
    }

    @Test
    public void testQuailifiersOfSpecializingdNestedClass() {
        testQualifiersOfSpecializedBean(StaticNestedClassesParent.StaticSpecializationBean.class,
                StaticNestedClassesParent.StaticMockSpecializationBean.class);
    }

    @Test
    public void testQuailifiersOfSpecializingClass() {
        testQualifiersOfSpecializedBean(SpecializationBean.class, MockSpecializationBean.class);
    }

    private void testQualifiersOfSpecializedBean(Class<?> specializedClass, Class<?> specializingClass) {
        Set<Bean<?>> specializationBeans = beanManager.getBeans(specializedClass, new Admin.AdminLiteral());
        assertEquals(1, specializationBeans.size());

        Bean<?> bean = specializationBeans.iterator().next();
        assertTrue(bean.getTypes().contains(specializingClass));
        Set<Annotation> qualifiers = bean.getQualifiers();
        assertEquals(2, qualifiers.size());
        assertTrue(qualifiers.contains(Any.Literal.INSTANCE));
        assertTrue(qualifiers.contains(new Admin.AdminLiteral()));
    }

}
