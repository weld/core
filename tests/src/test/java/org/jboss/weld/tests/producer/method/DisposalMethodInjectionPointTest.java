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
package org.jboss.weld.tests.producer.method;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class DisposalMethodInjectionPointTest extends AbstractWeldTest
{
   @Test(groups = { "broken" })
   // WELD-358
   public void test()
   {
      BarProducer.reset();
      Bean<BarConsumer> barConsumerBean = getBean(BarConsumer.class);
      CreationalContext<BarConsumer> ctx = getCurrentManager().createCreationalContext(barConsumerBean);
      BarConsumer barConsumer = barConsumerBean.create(ctx);
      assert BarProducer.getProducedInjection().getName().equals("bar");
      Bar bar = barConsumer.getBar();
      barConsumerBean.destroy(barConsumer, ctx);
      assert BarProducer.getDisposedBar() == bar;
      assert BarProducer.getDisposedInjection().getName().equals("bar");
   }
}
