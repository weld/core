/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.repeatable;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class Observer {

    private final Set<String> all = new HashSet<>();
    private final Set<String> foo = new HashSet<>();
    private final Set<String> fooBar = new HashSet<>();
    private final Set<String> fooBarBaz = new HashSet<>();
    private final Set<String> fooQux = new HashSet<>();

    public void observeAll(@Observes String event) {
        this.all.add(event);
    }

    public void observeFoo(@Observes @RepeatableQualifier("foo") String event) {
        this.foo.add(event);
    }

    public void observeFooBar(@Observes @RepeatableQualifier("foo") @RepeatableQualifier("bar") String event) {
        this.fooBar.add(event);
    }

    public void observeFooBarBaz(
            @Observes @RepeatableQualifier("foo") @RepeatableQualifier("bar") @RepeatableQualifier("baz") String event) {
        this.fooBarBaz.add(event);
    }

    public void observeFooQux(@Observes @RepeatableQualifier("foo") @RepeatableQualifier("qux") String event) {
        this.fooQux.add(event);
    }

    public Set<String> getAll() {
        return all;
    }

    public Set<String> getFoo() {
        return foo;
    }

    public Set<String> getFooBar() {
        return fooBar;
    }

    public Set<String> getFooBarBaz() {
        return fooBarBaz;
    }

    public Set<String> getFooQux() {
        return fooQux;
    }

    public void reset() {
        all.clear();
        foo.clear();
        fooBar.clear();
        fooBarBaz.clear();
        fooQux.clear();
    }
}
