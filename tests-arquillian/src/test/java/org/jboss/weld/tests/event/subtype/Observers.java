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
package org.jboss.weld.tests.event.subtype;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class Observers {

    private Foo foo;
    private Bar bar;
    private Baz<?> baz;

    void observeFoo(@Observes Foo foo) {
        this.foo = foo;
    }

    void observeBar(@Observes Bar bar) {
        this.bar = bar;
    }

    void observeBaz(@Observes Baz<?> baz) {
        this.baz = baz;
    }

    Bar getBar() {
        return bar;
    }

    Foo getFoo() {
        return foo;
    }

    Baz<?> getBaz() {
        return baz;
    }

    void reset() {
        this.foo = null;
        this.bar = null;
        this.baz = null;
    }

}
