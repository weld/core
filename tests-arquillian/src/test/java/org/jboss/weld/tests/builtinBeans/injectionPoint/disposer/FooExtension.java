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
package org.jboss.weld.tests.builtinBeans.injectionPoint.disposer;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import org.jboss.weld.injection.ForwardingInjectionPoint;

/**
 * Wraps injection points of Foo with a non-serializable wrapper. Later on we test that even though injection points are not
 * serializable, the container can deal with that by providing a serializable proxy around injected {@link InjectionPoint}
 * instances.
 * 
 * @author Jozef Hartinger
 * 
 */
public class FooExtension implements Extension {

    private boolean wrapped;

    void wrapFooInjectionPoints(@Observes ProcessInjectionPoint<Foo, Bar> event) {
        final InjectionPoint delegate = event.getInjectionPoint();
        event.setInjectionPoint(new ForwardingInjectionPoint() {

            @Override
            protected InjectionPoint delegate() {
                return delegate;
            }
        });
        wrapped = true;
    }

    public boolean isWrapped() {
        return wrapped;
    }
}
