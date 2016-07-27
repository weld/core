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
package org.jboss.weld.tests.event.parameterized;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class EventObserver {

    private boolean integerListFooableObserved;
    private boolean stringListFooableObserved;
    private boolean integerListFooObserved;
    private boolean integerListBarObserved;
    private boolean bazObserved;
    private boolean characterListObserved;
    private boolean integerFooObserved;

    public void observeIntegerFooable(@Observes Fooable<List<Integer>> event) {
        this.integerListFooableObserved = true;
    }

    public void observeStringFooable(@Observes Fooable<List<String>> event) {
        this.stringListFooableObserved = true;
    }

    public void observeListIntegerFoo(@Observes Foo<List<Integer>> event) {
        this.integerListFooObserved = true;
    }

    public void observeIntegerBar(@Observes Bar<List<Integer>> event) {
        this.integerListBarObserved = true;
    }

    public void observeBaz(@Observes Baz baz) {
        this.bazObserved = true;
    }

    public void observeCharacterList(@Observes List<Character> event) {
        this.characterListObserved = true;
    }

    public void observeIntegerFoo(@Observes Foo<? extends Number> event) {
        this.integerFooObserved = true;
    }

    public boolean isStringListFooableObserved() {
        return stringListFooableObserved;
    }

    public boolean isIntegerListFooObserved() {
        return integerListFooObserved;
    }

    public boolean isIntegerListBarObserved() {
        return integerListBarObserved;
    }

    public boolean isIntegerListFooableObserved() {
        return integerListFooableObserved;
    }

    public boolean isBazObserved() {
        return bazObserved;
    }

    public boolean isIntegerFooObserved() {
        return integerFooObserved;
    }

    public boolean isCharacterListObserved() {
        return characterListObserved;
    }

    public void reset() {
        this.integerListFooableObserved = false;
        this.stringListFooableObserved = false;
        this.integerListBarObserved = false;
        this.integerListFooObserved = false;
        this.bazObserved = false;
        this.characterListObserved = false;
        this.integerFooObserved = false;
    }

}
