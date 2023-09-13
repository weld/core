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
package org.jboss.weld.tests.metadata.beanattributes.qualifiers;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Named;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class DefaultQualifierTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DefaultQualifierTest.class))
                .addClasses(Foo.class, Bar.class, Baz.class, Qux.class, Juicy.class, Utils.class, BeanUtilities.class);
    }

    @Test
    public void testFooHasDefaultQualifier(BeanManager beanManager) {
        assertHasDefaultQualifier(beanManager, Foo.class, Any.Literal.INSTANCE, Default.Literal.INSTANCE);
    }

    @Test
    public void testBarHasDefaultQualifier(BeanManager beanManager) {
        assertHasDefaultQualifier(beanManager, Bar.class, Any.Literal.INSTANCE, Default.Literal.INSTANCE,
                new NamedLiteral("bar"));
    }

    @Test
    public void testBazHasDefaultQualifier(BeanManager beanManager) {
        assertHasDefaultQualifier(beanManager, Baz.class, Any.Literal.INSTANCE, Default.Literal.INSTANCE);
    }

    @Test
    public void testQuxDoesNotHaveDefaultQualifier(BeanManager beanManager) {
        Bean<Qux> bean = Utils.getBean(beanManager, Qux.class, Juicy.Literal.INSTANCE);
        BeanUtilities.verifyQualifiers(bean, Any.Literal.INSTANCE, Juicy.Literal.INSTANCE);
    }

    private <T> void assertHasDefaultQualifier(BeanManager beanManager, Class<T> beanType, Annotation... expectedAnnotations) {
        // The lookup will fail if the bean does not have the default qualifier
        Bean<T> bean = Utils.getBean(beanManager, beanType);
        BeanUtilities.verifyQualifiers(bean, expectedAnnotations);
    }

    @Any
    @Dependent
    static class Foo {
    }

    @Named
    @Any
    @Dependent
    static class Bar {
    }

    @Dependent
    static class Baz {
    }

    @Juicy
    @Dependent
    static class Qux {
    }

}
