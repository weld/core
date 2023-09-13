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
package org.jboss.weld.tests.annotatedType;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@RunWith(Arquillian.class)
public class ExampleTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExampleTest.class))
                .addPackage(ExampleTest.class.getPackage());
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testAnnotatedCallableGetParameters() throws Exception {
        AnnotatedType<Bean> type = beanManager.createAnnotatedType(Bean.class);

        assertNoAnnotations(type);

        Assert.assertEquals(1, type.getConstructors().size());
        for (AnnotatedConstructor<Bean> ctor : type.getConstructors()) {
            assertNoAnnotations(ctor);

            for (AnnotatedParameter<Bean> param : ctor.getParameters()) {
                assertNoAnnotations(param);
            }
        }

        Assert.assertEquals(1, type.getMethods().size());
        for (AnnotatedMethod<? super Bean> method : type.getMethods()) {
            assertNoAnnotations(method);

            for (AnnotatedParameter<? super Bean> param : method.getParameters()) {
                assertNoAnnotations(param);
            }
        }

        Assert.assertEquals(1, type.getFields().size());
        for (AnnotatedField<? super Bean> field : type.getFields()) {
            assertNoAnnotations(field);
        }
    }

    private void assertNoAnnotations(Annotated annotated) {
        Assert.assertEquals(0, annotated.getAnnotations().size());
    }
}
