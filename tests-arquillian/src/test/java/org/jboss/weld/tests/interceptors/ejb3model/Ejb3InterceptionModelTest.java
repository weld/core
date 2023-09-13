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

package org.jboss.weld.tests.interceptors.ejb3model;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marius Bogoevici
 */
@RunWith(Arquillian.class)
public class Ejb3InterceptionModelTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(Ejb3InterceptionModelTest.class))
                .addPackage(Ejb3InterceptionModelTest.class.getPackage());
    }

    //@Before // ARQ-391
    public void reset() {
        Ball.played = false;
        Goalkeeper.caught = false;
        Defender.defended = false;
        Ball.aroundInvoke = false;
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testSimpleInterceptor() {
        reset();
        Bean bean = beanManager.getBeans(Ball.class).iterator().next();
        CreationalContext creationalContext = beanManager.createCreationalContext(bean);
        Ball ball = (Ball) bean.create(creationalContext);
        ball.shoot();
        assert Defender.defended;
        assert Ball.played;
        assert !Goalkeeper.caught;
        assert Ball.aroundInvoke;
    }

    @Test
    public void testSimpleInterceptor2() {
        reset();
        Bean bean = beanManager.getBeans(Ball.class).iterator().next();
        CreationalContext creationalContext = beanManager.createCreationalContext(bean);
        Ball ball = (Ball) bean.create(creationalContext);
        ball.pass();
        assert Defender.defended;
        assert Ball.played;
        assert Goalkeeper.caught;
        assert Ball.aroundInvoke;
    }

    @Test
    public void testSimpleInterceptor3() {
        reset();
        Bean bean = beanManager.getBeans(Ball.class).iterator().next();
        CreationalContext creationalContext = beanManager.createCreationalContext(bean);
        Ball ball = (Ball) bean.create(creationalContext);
        ball.lob();
        assert !Defender.defended;
        assert Ball.played;
        assert Goalkeeper.caught;
        assert Ball.aroundInvoke;
    }

}
