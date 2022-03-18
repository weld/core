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

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.junit.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class DisposesMethodCalledOnceTest {

    @Inject
    BeanManager beanManager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DisposesMethodCalledOnceTest.class)).addPackage(DisposesMethodCalledOnceTest.class.getPackage());
    }

    @Test
    public void testDisposerCalledOnce1() {
        ProducerBean.reset();
        beanManager.getEvent().select(String.class).fire("Hello");
        Assert.assertEquals("Disposer method called multiple times!", 1, ProducerBean.firstDisposerCalled.get());
    }

    @Test
    public void testDisposerCalledOnce2() {
        ProducerBean.reset();
        beanManager.getEvent().select(String.class).fire("Hello");
        Assert.assertEquals("Disposer method called multiple times!", 1, ProducerBean.secondDisposerCalled.get());
    }

    @Test
    public void testDisposerCalledOnce3() {
        ProducerBean.reset();
        beanManager.getEvent().select(String.class).fire("Hello");
        Assert.assertEquals("Disposer method called multiple times!", 1, ProducerBean.thirdDisposerCalled.get());
    }

    @Test
    public void testDisposerCalledOnce4() {
        ProducerBean.reset();
        beanManager.getEvent().select(String.class).fire("Hello");
        Assert.assertEquals("Disposer method called multiple times!", 1, ProducerBean.forthDisposerCalled.get());
    }

    @Test
    public void testDisposerCalledOnce5() {
        ProducerBean.reset();
        beanManager.getEvent().select(String.class).fire("Hello");
        Assert.assertEquals("Disposer method called multiple times!", 1, ProducerBean.fifthDisposerCalled.get());
    }

    @Test
    public void testDisposerCalledOnce6() {
        ProducerBean.reset();
        beanManager.getEvent().select(String.class).fire("Hello");
        Assert.assertEquals("Disposer method called multiple times!", 1, ProducerBean.sixthDisposerCalled.get());
    }

}
