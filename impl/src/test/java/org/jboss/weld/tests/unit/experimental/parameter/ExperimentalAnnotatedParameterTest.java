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
package org.jboss.weld.tests.unit.experimental.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import junit.framework.Assert;

import org.jboss.weld.experimental.ExperimentalAnnotatedParameter;
import org.junit.Test;

/**
 * Test for the default method implementation {@link ExperimentalAnnotatedParameter#getJavaParameter()}
 * @author Jozef Hartinger
 *
 */
public class ExperimentalAnnotatedParameterTest {

    @SuppressWarnings("unused")
    private static void method(String param1, Integer param2) {
    }

    @Test
    public void testGetJavaParameter() {
        Parameter param1 = new MockAnnotatedParameter(0).getJavaParameter();
        Parameter param2 = new MockAnnotatedParameter(1).getJavaParameter();

        Assert.assertEquals(String.class, param1.getParameterizedType());
        Assert.assertEquals(Integer.class, param2.getParameterizedType());
    }

    private static class MockAnnotatedParameter implements ExperimentalAnnotatedParameter<ExperimentalAnnotatedParameterTest> {

        private final int position;

        public MockAnnotatedParameter(int position) {
            this.position = position;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public AnnotatedCallable<ExperimentalAnnotatedParameterTest> getDeclaringCallable() {
            return MOCK_METHOD;
        }

        @Override
        public Type getBaseType() {
            return null;
        }

        @Override
        public Set<Type> getTypeClosure() {
            return null;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return null;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return null;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return false;
        }
    }

    private static final AnnotatedMethod<ExperimentalAnnotatedParameterTest> MOCK_METHOD = new AnnotatedMethod<ExperimentalAnnotatedParameterTest>() {

        @Override
        public List<AnnotatedParameter<ExperimentalAnnotatedParameterTest>> getParameters() {
            return Collections.emptyList();
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public AnnotatedType<ExperimentalAnnotatedParameterTest> getDeclaringType() {
            return null;
        }

        @Override
        public Type getBaseType() {
            return null;
        }

        @Override
        public Set<Type> getTypeClosure() {
            return null;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return null;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return null;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return false;
        }

        @Override
        public Method getJavaMember() {
            try {
                return ExperimentalAnnotatedParameterTest.class.getDeclaredMethod("method", String.class, Integer.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
    };
}
