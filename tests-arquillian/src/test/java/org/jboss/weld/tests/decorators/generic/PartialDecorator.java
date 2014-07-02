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

package org.jboss.weld.tests.decorators.generic;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

/**
 * @author Marius Bogoevici
 */
@Decorator
public class PartialDecorator<T extends String> implements Decorated<T> {

    @Inject
    @Delegate
    GenericBean<T> delegate;

    static boolean decoratedInvoked = false;

    static boolean notDecoratedInvoked = false;

    public T decoratedEcho(T parameter) {
        decoratedInvoked = true;
        return delegate.decoratedEcho(parameter);
    }

    /**
     * Should not be invoked
     */
    public T notDecoratedEcho(T parameter) {
        notDecoratedInvoked = true;
        return delegate.notDecoratedEcho(parameter);
    }
}
