/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * Simple test which invokes a method directly on a normal scoped producer
 * bean to ensure that it's proxy is for that bean and not the product
 * of a producer method.
 * 
 * @author David Allen
 *
 */
@Artifact
public class ProducerBeanInvocationTest extends AbstractWeldTest
{
   @Test
   // WELD-546
   public void test()
   {
      FooProducer fooProducer = this.getReference(FooProducer.class);
      assert fooProducer.ping();
   }
}
