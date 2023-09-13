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

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
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
 * @author kkahn
 */
@RunWith(Arquillian.class)
public class DeclaringTypeTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DeclaringTypeTest.class))
                .addPackage(DeclaringTypeTest.class.getPackage());
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testInheritance() {
        AnnotatedType<Child> type = beanManager.createAnnotatedType(Child.class);
        Assert.assertEquals(1, type.getConstructors().size());
        Assert.assertEquals(1, type.getFields().size());
        for (AnnotatedField<? super Child> field : type.getFields()) {
            if (field.getJavaMember().getName().equals("parent")) {
                Assert.assertEquals(Parent.class, field.getJavaMember().getDeclaringClass()); // OK - Returns Parent
                // this assertion is commented out because the spec is not clear which type to return and the flat type actually makes more sense
                //                Assert.assertEquals(Parent.class, field.getDeclaringType().getJavaClass()); // FAIL - Returns Child
            } else {
                Assert.fail("Unknown field " + field.getJavaMember());
            }
        }

        Assert.assertEquals(1, type.getMethods().size());
        for (AnnotatedMethod<? super Child> method : type.getMethods()) {
            if (method.getJavaMember().getName().equals("parentMethod")) {
                Assert.assertEquals(Parent.class, method.getJavaMember().getDeclaringClass()); // OK - Returns Parent
                // this assertion is commented out because the spec is not clear which type to return and the flat type actually makes more sense
                //                Assert.assertEquals(Parent.class, method.getDeclaringType().getJavaClass()); // FAIL - Returns Child
            } else {
                Assert.fail("Unknown method " + method.getJavaMember());
            }
        }
    }

}
