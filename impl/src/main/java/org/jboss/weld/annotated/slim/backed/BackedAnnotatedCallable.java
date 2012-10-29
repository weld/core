/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.annotated.slim.backed;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.resources.ClassTransformer;

public abstract class BackedAnnotatedCallable<X, M extends Member> extends BackedAnnotatedMember<X> implements AnnotatedCallable<X> {

    private final List<AnnotatedParameter<X>> parameters;

    public BackedAnnotatedCallable(M member, Type baseType, BackedAnnotatedType<X> declaringType, ClassTransformer transformer) {
        super(baseType, declaringType, transformer);
        this.parameters = initParameters(member, transformer);
    }

    protected abstract List<AnnotatedParameter<X>> initParameters(M member, ClassTransformer transformer);

    @Override
    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
    }

}
