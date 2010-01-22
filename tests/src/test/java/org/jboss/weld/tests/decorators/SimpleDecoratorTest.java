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

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Artifact
@BeansXml("beans.xml")
public class SimpleDecoratorTest extends AbstractWeldTest
{
   @Test
   public void testSimpleDecorator()
   {
      SimpleBean simpleBean = getReference(SimpleBean.class);
      
      resetDecorators();
      assert simpleBean.echo1(1) == 1;
      assertDecorators(true, false, false);
      assert simpleBean.isInvoked();
      
      resetDecorators();
      assert simpleBean.echo2(2) == 2;
      assertDecorators(false, true, false);
      assert simpleBean.isInvoked();

      //Only SimpleDecorator1 gets invoked, although I think SimpleDecorator2 should get invoked too
      resetDecorators();
      assert simpleBean.echo3(3) == 3;
      assertDecorators(false, false, true);

      assert simpleBean.isInvoked();

      resetDecorators();
      assert simpleBean.echo4(4) == 4; 
      assertDecorators(false, false, false);

      assert simpleBean.isInvoked();
   }
   
   private void resetDecorators()
   {
      SimpleDecorator1.reset();
      SimpleDecorator2.reset();
   }
   
   private void assertDecorators(boolean echo1, boolean echo2, boolean echo3)
   {
      assert SimpleDecorator1.echo1 == echo1;
      assert SimpleDecorator1.echo3 == echo3;
      assert SimpleDecorator2.echo2 == echo2;
      assert SimpleDecorator2.echo3 == echo3;
   }
}

