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
package org.jboss.weld.tests.extensions.multipleBeans;


import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * Tests that it is possible to add multiple beans with the same java class type
 * through the SPI
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
@Extension("javax.enterprise.inject.spi.Extension")
@Classes(packages = { "org.jboss.weld.tests.util.annotated" })
public class MultipleBeansTest extends AbstractWeldTest
{

   @Test
   public void testFormatterRegistered()
   {
      // test that we have added two beans with the same qualifiers
      assert getBeans(BlogFormatter.class).size() == 2;
      // test that the beans which have different producer methods produce
      // different values
      assert getReference(String.class, new FormattedBlogLiteral("Bob")).equals("+Bob's content+");
      assert getReference(String.class, new FormattedBlogLiteral("Barry")).equals("+Barry's content+");
   }

   @Test
   public void testBlogConsumed()
   {
      // test that the two different BlogConsumers have been registered
      // correctly
      BlogConsumer consumer = getReference(BlogConsumer.class, new ConsumerLiteral("Barry"));
      assert consumer.blogContent.equals("+Barry's content+");
      consumer = getReference(BlogConsumer.class, new ConsumerLiteral("Bob"));
      assert consumer.blogContent.equals("+Bob's content+");
   }

   /**
    * Apparently it is not possible to add two beans that are exactly the same.
    * Even though this is not very useful it should still be possible.
    * 
    */
   @Test(groups = { "broken" })
   public void testTwoBeansExactlyTheSame()
   {
      assert getBeans(UselessBean.class).size() == 2;
   }

}
