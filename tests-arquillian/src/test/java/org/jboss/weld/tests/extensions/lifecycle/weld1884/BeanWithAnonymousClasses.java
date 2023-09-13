/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.lifecycle.weld1884;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;

@Dependent
public class BeanWithAnonymousClasses {

    static ThreadLocal<BeanWithAnonymousClasses> threadLocal = new ThreadLocal<BeanWithAnonymousClasses>() {
        @Override
        protected BeanWithAnonymousClasses initialValue() {
            return new BeanWithAnonymousClasses();
        }

    };

    @SuppressWarnings("serial")
    static Serializable serializable = new Serializable() {

    };

    @SuppressWarnings("serial")
    Serializable serializable2 = new Serializable() {

    };
}
