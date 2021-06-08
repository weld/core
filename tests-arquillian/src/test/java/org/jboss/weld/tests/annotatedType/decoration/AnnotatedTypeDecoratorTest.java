/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.annotatedType.decoration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@RunWith(Arquillian.class)
public class AnnotatedTypeDecoratorTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AnnotatedTypeDecoratorTest.class))
                .addPackage(AnnotatedTypeDecoratorTest.class.getPackage());
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testAnnotationDecorator() throws Exception {
        NotAnnotated.reset();
        AnnotatedType<NotAnnotated> type = beanManager.createAnnotatedType(NotAnnotated.class);
        checkAnnotations(type, new NoAnnotationsChecker());

        type = new MockAnnotatedType<NotAnnotated>(type);
        checkAnnotations(type, new MockAnnotationsChecker());

        NonContextual<NotAnnotated> nonContextual = new NonContextual<NotAnnotated>(beanManager, type);
        NotAnnotated instance = nonContextual.create();
        Assert.assertNotNull(instance);
        nonContextual.postConstruct(instance);

        Assert.assertNotNull(instance.getFromField());
        Assert.assertNotNull(NotAnnotated.getFromConstructor());
        Assert.assertNotNull(NotAnnotated.getFromInitializer());
    }

    private void checkAnnotations(AnnotatedType<NotAnnotated> type, TypeChecker checker) {
        checker.assertAnnotations(type);

        Assert.assertEquals(1, type.getConstructors().size());

        checker.assertAnnotations(type.getConstructors().iterator().next());
        checker.assertAnnotations(type.getConstructors().iterator().next().getParameters().get(0));

        Assert.assertEquals(3, type.getFields().size());
        for (AnnotatedField<? super NotAnnotated> field : type.getFields()) {
            if (field.getJavaMember().getName().equals("fromField")) {
                checker.assertAnnotations(field);
            } else {
                Assert.assertEquals(0, field.getAnnotations().size());
            }
        }
        Assert.assertEquals(5, type.getMethods().size());
        checker.assertAnnotations(type.getMethods().iterator().next());
    }


    interface TypeChecker {
        void assertAnnotations(Annotated annotated);
    }

    class NoAnnotationsChecker implements TypeChecker {

        public void assertAnnotations(Annotated annotated) {
            Assert.assertEquals(0, annotated.getAnnotations().size());
        }
    }

    class MockAnnotationsChecker implements TypeChecker {

        public void assertAnnotations(Annotated annotated) {
            if (annotated instanceof MockAnnotatedCallable) {
                Assert.assertEquals(1, annotated.getAnnotations().size());
                Assert.assertTrue(annotated.isAnnotationPresent(Inject.class));
            } else if (annotated instanceof MockAnnotatedField<?>) {
                Assert.assertEquals(1, annotated.getAnnotations().size());
                Assert.assertTrue(annotated.isAnnotationPresent(Inject.class));
            }
        }
    }

    public class NonContextual<T> {

        final InjectionTarget<T> it;
        final BeanManager manager;
        CreationalContext<T> cc;

        public NonContextual(BeanManager manager, AnnotatedType<T> type) {
            this.manager = manager;
            this.it = manager.getInjectionTargetFactory(type).createInjectionTarget(null);
            cc = manager.createCreationalContext(null);
        }

        public T create() {
            return it.produce(cc);
        }

        public CreationalContext<T> postConstruct(T instance) {
            it.inject(instance, cc);
            it.postConstruct(instance);
            return cc;
        }

        public void preDestroy(T instance) {
            it.preDestroy(instance);
        }
    }
}
