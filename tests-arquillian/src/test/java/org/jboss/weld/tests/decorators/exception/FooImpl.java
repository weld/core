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
package org.jboss.weld.tests.decorators.exception;

import jakarta.enterprise.context.Dependent;

@Dependent
public class FooImpl implements Foo {

    @Override
    public void throwCheckedException1() throws CheckedException {
        throw new CheckedException();
    }

    @Override
    public void throwCheckedException2() throws CheckedException {
        throw new CheckedException();
    }

    @Override
    public void throwCheckedException3() throws CheckedException {
        throw new CheckedException();
    }

    @Override
    public void throwCheckedException4() throws CheckedException {
        throw new CheckedException();
    }

    @Override
    public void throwRuntimeException1() {
        throw new CustomRuntimeException();
    }

    @Override
    public void throwRuntimeException2() {
        throw new CustomRuntimeException();
    }

    @Override
    public void throwRuntimeException3() {
        throw new CustomRuntimeException();
    }

    @Override
    public void throwRuntimeException4() {
        throw new CustomRuntimeException();
    }
}
