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
package org.jboss.weld.tests.decorators;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@RunWith(Arquillian.class)
public class SimpleDecoratorTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .decorate(SimpleDecorator1.class, SimpleDecorator2.class)
         .addPackage(SimpleDecoratorTest.class.getPackage());
   }

   @Test
   public void testSimpleDecorator(SimpleBean simpleBean)
   {
      resetDecorators();
      Assert.assertEquals(1, simpleBean.echo1(1));
      assertDecorators(true, false, false);
      Assert.assertTrue(simpleBean.isInvoked());

      resetDecorators();
      Assert.assertEquals(2, simpleBean.echo2(2));
      assertDecorators(false, true, false);
      Assert.assertTrue(simpleBean.isInvoked());

      //Only SimpleDecorator1 gets invoked, although I think SimpleDecorator2 should get invoked too
      resetDecorators();
      Assert.assertEquals(3, simpleBean.echo3(3));
      assertDecorators(false, false, true);

      Assert.assertTrue(simpleBean.isInvoked());

      resetDecorators();
      Assert.assertEquals(4, simpleBean.echo4(4));
      assertDecorators(false, false, false);

      Assert.assertTrue(simpleBean.isInvoked());
   }

   private void resetDecorators()
   {
      SimpleDecorator1.reset();
      SimpleDecorator2.reset();
   }

   private void assertDecorators(boolean echo1, boolean echo2, boolean echo3)
   {
      Assert.assertEquals(echo1, SimpleDecorator1.echo1);
      Assert.assertEquals(echo2, SimpleDecorator2.echo2);
      Assert.assertEquals(echo3, SimpleDecorator2.echo3);
   }
}
