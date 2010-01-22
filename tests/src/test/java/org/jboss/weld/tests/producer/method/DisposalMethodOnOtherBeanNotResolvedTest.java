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
public class DisposalMethodOnOtherBeanNotResolvedTest extends AbstractWeldTest
{
   @Test
   public void test()
   {
      FooDisposer.reset();
      FooProducer.reset();
      Bean<Foo> bean = getBean(Foo.class);
      CreationalContext<Foo> ctx = getCurrentManager().createCreationalContext(bean);
      Foo instance = bean.create(ctx);
      assert instance.getBlah().equals("foo!");
      bean.destroy(instance, ctx);
      assert !FooDisposer.isDisposed();
      assert FooProducer.isDisposed();
   }

}
