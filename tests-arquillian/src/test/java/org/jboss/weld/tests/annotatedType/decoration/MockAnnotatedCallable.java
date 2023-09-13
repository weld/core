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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class MockAnnotatedCallable<X> extends MockAnnotatedMember<X> implements AnnotatedCallable<X> {

    private static class InjectLiteral extends AnnotationLiteral<Inject> implements Inject {

    }

    private final static Inject INITIALIZER = new InjectLiteral();

    private final List<AnnotatedParameter<X>> parameters;

    public MockAnnotatedCallable(Annotated delegate) {
        super(delegate);
        parameters = initialiseParameters();
    }

    @Override
    AnnotatedCallable<X> getDelegate() {
        return (AnnotatedCallable<X>) super.getDelegate();
    }

    private List<AnnotatedParameter<X>> initialiseParameters() {
        int size = getDelegate().getParameters().size();
        List<AnnotatedParameter<X>> params = new ArrayList<AnnotatedParameter<X>>(size);
        if (size > 0) {
            for (AnnotatedParameter<X> param : getDelegate().getParameters()) {
                params.add(new MockAnnotatedParameter<X>(param, this));
            }
        }
        return params;
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (annotationType == Inject.class) {
            return (T) INITIALIZER;
        }
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return Collections.singleton((Annotation) INITIALIZER);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return annotationType == Inject.class;
    }
}
