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
package org.jboss.weld.tests.annotatedType.decoration;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class NotAnnotated {
    private static Foo fromConstructor;

    private static Foo fromInitialize;

    private Foo fromField;

    public NotAnnotated(Foo foo) {
        fromConstructor = foo;
    }

    public void initialize(Foo foo) {
        fromInitialize = foo;
    }

    /**
     * @return the beanFromConstructor
     */
    public static Foo getFromConstructor() {
        return fromConstructor;
    }

    /**
     * @return the beanFromInitialize
     */
    public static Foo getFromInitializer() {
        return fromInitialize;
    }

    /**
     * @return the fromField
     */
    public Foo getFromField() {
        return fromField;
    }

    public static void reset() {
        fromConstructor = null;
        fromInitialize = null;
    }
}
