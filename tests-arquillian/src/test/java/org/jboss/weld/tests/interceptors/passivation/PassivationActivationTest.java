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

package org.jboss.weld.tests.interceptors.passivation;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.exceptions.DefinitionException;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Marius Bogoevici
 */
@RunWith(Arquillian.class)
public class PassivationActivationTest {

    @ShouldThrowException(DefinitionException.class)
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .intercept(Goalkeeper.class, Defender.class, PassivationActivation.class)
                .addPackage(PassivationActivationTest.class.getPackage());
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testPassivationAndActivation() throws Exception {
        Bean bean = beanManager.getBeans(Ball.class).iterator().next();
        CreationalContext creationalContext = beanManager.createCreationalContext(bean);

        PassivationActivationInterceptor.initialMessage = "Goal!";

        Ball ball = (Ball) bean.create(creationalContext);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(ball);

        PassivationActivationInterceptor oldInterceptor = PassivationActivationInterceptor.instance;

        PassivationActivationInterceptor.initialMessage = "Miss!";

        assert PassivationActivationInterceptor.prePassivateInvoked;
        assert !PassivationActivationInterceptor.postActivateInvoked;
        assert PassivationActivationInterceptor.instance != null;

        PassivationActivationInterceptor.prePassivateInvoked = false;
        PassivationActivationInterceptor.postActivateInvoked = false;
        PassivationActivationInterceptor.instance = null;

        ball = (Ball) new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())).readObject();

        assert !PassivationActivationInterceptor.prePassivateInvoked;
        assert PassivationActivationInterceptor.postActivateInvoked;
        assert PassivationActivationInterceptor.instance != null;
        assert PassivationActivationInterceptor.instance != oldInterceptor;
        assert PassivationActivationInterceptor.instance.message != null;
        assert PassivationActivationInterceptor.instance.message.equals(oldInterceptor.message);
    }
}
