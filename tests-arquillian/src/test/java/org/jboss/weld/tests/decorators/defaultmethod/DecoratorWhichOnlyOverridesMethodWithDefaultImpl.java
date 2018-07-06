/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.defaultmethod;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

/**
 * Note: this class should not override *any* other method than the one with a default implementation.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@Decorator
public abstract class DecoratorWhichOnlyOverridesMethodWithDefaultImpl implements Decorated {

    @Inject
    @Delegate
    Decorated delegate;

    static int decoratedInvocationCount = 0;

    public static void reset() {
        decoratedInvocationCount = 0;
    }

    @Override
    public void defaultDecorated() {
        decoratedInvocationCount++;
        delegate.defaultDecorated();
    }
}
