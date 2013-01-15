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
package org.jboss.weld.enums;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * Contains everything the container needs to know to manage an instance of a Java enum.
 *
 * @author Jozef Hartinger
 *
 * @param <T> enum type
 */
public class EnumInstanceContext<T extends Enum<?>> {

    private final T instance;
    private final InjectionTarget<T> injectionTarget;
    private final CreationalContext<T> ctx;

    public EnumInstanceContext(T instance, InjectionTarget<T> injector, CreationalContext<T> ctx) {
        this.instance = instance;
        this.injectionTarget = injector;
        this.ctx = ctx;
    }

    public void inject() {
        injectionTarget.inject(instance, ctx);
    }

    public void destroy() {
        injectionTarget.dispose(instance);
        ctx.release();
    }
}
