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
package org.jboss.weld.test.util.annotated;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;

/**
 * @author Stuart Douglas
 */
abstract class AbstractTestAnnotatedCallable<X, Y extends Member> extends AbstractTestAnnotatedMember<X, Y>
        implements AnnotatedCallable<X> {

    private final List<AnnotatedParameter<X>> parameters;

    protected AbstractTestAnnotatedCallable(AnnotatedType<X> declaringType, Y member, Class<?> memberType,
            Class<?>[] parameterTypes, TestAnnotationStore annotations,
            Map<Integer, TestAnnotationStore> parameterAnnotations) {
        super(declaringType, member, memberType, annotations);
        this.parameters = getAnnotatedParameters(this, parameterTypes, parameterAnnotations);
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public AnnotatedParameter<X> getParameter(int index) {
        return parameters.get(index);

    }

    private static <X, Y extends Member> List<AnnotatedParameter<X>> getAnnotatedParameters(
            AbstractTestAnnotatedCallable<X, Y> callable, Class<?>[] parameterTypes,
            Map<Integer, TestAnnotationStore> parameterAnnotations) {
        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>();
        int len = parameterTypes.length;
        for (int i = 0; i < len; ++i) {
            TestAnnotationBuilder builder = new TestAnnotationBuilder();
            if (parameterAnnotations != null && parameterAnnotations.containsKey(i)) {
                builder.addAll(parameterAnnotations.get(i));
            }
            TestAnnotatedParameter<X> p = new TestAnnotatedParameter<X>(callable, parameterTypes[i], i, builder.create());
            parameters.add(p);
        }
        return parameters;
    }

}
