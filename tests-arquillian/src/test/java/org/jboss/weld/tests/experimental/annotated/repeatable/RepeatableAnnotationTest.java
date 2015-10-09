/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.experimental.annotated.repeatable;

import static org.junit.Assert.assertArrayEquals;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.experimental.ExperimentalAnnotated;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testcase for WELD-1743
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class RepeatableAnnotationTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(RepeatableAnnotationTest.class)).addPackage(RepeatableAnnotationTest.class.getPackage());
    }

    @Test
    public void testGetAnnotationsByType(BeanManager manager) {
        ExperimentalAnnotated type = (ExperimentalAnnotated) manager.createAnnotatedType(DummyClass.class);
        assertArrayEquals(DummyClass.class.getAnnotationsByType(RepeatableAnnotation.class), type.getAnnotationsByType(RepeatableAnnotation.class).toArray());
        assertArrayEquals(DummyClass.class.getAnnotationsByType(RepeatableAnnotation.Container.class), type.getAnnotationsByType(RepeatableAnnotation.Container.class).toArray());
        assertArrayEquals(DummyClass.class.getAnnotationsByType(Named.class), type.getAnnotationsByType(Named.class).toArray());
        assertArrayEquals(DummyClass.class.getAnnotationsByType(Default.class), type.getAnnotationsByType(Default.class).toArray());
    }
}
