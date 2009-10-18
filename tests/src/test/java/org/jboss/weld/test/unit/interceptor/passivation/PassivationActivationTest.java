/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.test.unit.interceptor.passivation;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWeldTest;

import org.testng.annotations.Test;

/**
 * @author Marius Bogoevici
 */
@Artifact
@BeansXml("beans.xml")
public class PassivationActivationTest extends AbstractWeldTest
{

   @Test
   public void testPassivationAndActivation() throws Exception
   {
      Bean bean = getCurrentManager().getBeans(Ball.class).iterator().next();
      CreationalContext creationalContext = getCurrentManager().createCreationalContext(bean);

      PassivationActivationInterceptor.initialMessage = "Goal!";

      Ball ball = (Ball) bean.create(creationalContext);

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      new ObjectOutputStream(byteArrayOutputStream).writeObject(ball);

      PassivationActivationInterceptor oldInterceptor = PassivationActivationInterceptor.instance;

      PassivationActivationInterceptor.initialMessage = "Miss!";

      assert PassivationActivationInterceptor.prePassivateInvoked;
      assert !PassivationActivationInterceptor.postActivateInvoked;
      assert PassivationActivationInterceptor.instance  != null;

      PassivationActivationInterceptor.prePassivateInvoked = false;
      PassivationActivationInterceptor.postActivateInvoked = false;
      PassivationActivationInterceptor.instance = null;

      ball = (Ball)new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())).readObject();

      assert !PassivationActivationInterceptor.prePassivateInvoked;
      assert PassivationActivationInterceptor.postActivateInvoked;
      assert PassivationActivationInterceptor.instance  != null;
      assert PassivationActivationInterceptor.instance != oldInterceptor;
      assert PassivationActivationInterceptor.instance.message != null;
      assert PassivationActivationInterceptor.instance.message.equals(oldInterceptor.message);
   }
}
