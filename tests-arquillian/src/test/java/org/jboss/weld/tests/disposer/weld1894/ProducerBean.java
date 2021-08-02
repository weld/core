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
package org.jboss.weld.tests.disposer.weld1894;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.TransientReference;

@Dependent
public class ProducerBean {

    public static AtomicInteger firstDisposerCalled = new AtomicInteger();
    public static AtomicInteger secondDisposerCalled = new AtomicInteger();
    public static AtomicInteger thirdDisposerCalled = new AtomicInteger();
    public static AtomicInteger forthDisposerCalled = new AtomicInteger();
    public static AtomicInteger fifthDisposerCalled = new AtomicInteger();
    public static AtomicInteger sixthDisposerCalled = new AtomicInteger();

    @Produces
    @DummyQualifier("A")
    public FirstBean produceA() {
        return new FirstBean();
    }

    @Produces
    @DummyQualifier("B")
    public FirstBean produceB() {
        return new FirstBean();
    }

    @Produces
    @DummyQualifier("C")
    public FirstBean produceC(@DummyQualifier("D") SecondBean secondBean) {
        secondBean.ping();
        return new FirstBean();
    }

    @Produces
    @DummyQualifier("E")
    public FirstBean produceE(@TransientReference @DummyQualifier("F") SecondBean secondBean) {
        secondBean.ping();
        return new FirstBean();
    }

    @Produces
    @DummyQualifier("D")
    public SecondBean produceD() {
        return new SecondBean();
    }

    @Produces
    @DummyQualifier("F")
    public SecondBean produceF() {
        return new SecondBean();
    }

    public void disposeA(@Disposes @DummyQualifier("A") FirstBean bean) {
        firstDisposerCalled.incrementAndGet();
    }

    public void disposeB(@Disposes @DummyQualifier("B") FirstBean bean) {
        secondDisposerCalled.incrementAndGet();
    }

    public void disposeC(@Disposes @DummyQualifier("C") FirstBean bean) {
        thirdDisposerCalled.incrementAndGet();
    }

    public void disposeE(@Disposes @DummyQualifier("E") FirstBean bean) {
        sixthDisposerCalled.incrementAndGet();
    }

    public void disposeD(@Disposes @DummyQualifier("D") SecondBean bean) {
        forthDisposerCalled.incrementAndGet();
    }

    public void disposeF(@Disposes @DummyQualifier("F") SecondBean bean) {
        fifthDisposerCalled.incrementAndGet();
    }

    public static void reset() {
        firstDisposerCalled = new AtomicInteger();
        secondDisposerCalled = new AtomicInteger();
        thirdDisposerCalled = new AtomicInteger();
        forthDisposerCalled = new AtomicInteger();
        fifthDisposerCalled = new AtomicInteger();
        sixthDisposerCalled = new AtomicInteger();
    }
}
