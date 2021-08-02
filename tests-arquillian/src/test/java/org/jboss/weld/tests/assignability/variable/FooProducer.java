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
package org.jboss.weld.tests.assignability.variable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class FooProducer {

    @Produces
    public <T extends Exception> Foo<T> produceAssignableFoo() {
        return new Foo<T>("AssignableFoo");
    }

    @Produces
    public <T extends UnsupportedOperationException> Foo<T> produceNonAssignableFoo() {
        return new Foo<T>("NonAssignableFoo");
    }

    @Produces
    @SuppressWarnings("rawtypes")
    public Foo produceRawFoo() {
        return new Foo("RawFoo");
    }

    @Produces
    public <A extends Number, B extends A> Foo<B> produceNumberFoo() {
        return new Foo<B>("NumberFoo");
    }

}
