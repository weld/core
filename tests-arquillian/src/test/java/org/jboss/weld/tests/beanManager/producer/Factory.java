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
package org.jboss.weld.tests.beanManager.producer;

import static org.junit.Assert.assertNotNull;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

@Vetoed
public class Factory {

    public static final Toy WOODY = new Toy("Woody");

    public static final SpaceSuit<?> INVALID_FIELD1 = new SpaceSuit<Object>();
    @Inject
    public static final Toy INVALID_FIELD2 = null;

    public final Object INVALID_FIELD3 = null;

    public static Toy getBuzz(BeanManager manager, SpaceSuit<Toy> suit) {
        assertNotNull(manager);
        assertNotNull(suit);
        return new Toy("Buzz Lightyear");
    }

    public static <T> T invalidProducerMethod1(T t) {
        return null;
    }

    public Toy invalidProducerMethod2() {
        return new Toy("nonStaticNonBean");
    }
}
