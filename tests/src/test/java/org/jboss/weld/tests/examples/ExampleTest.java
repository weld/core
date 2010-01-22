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
package org.jboss.weld.tests.examples;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ExampleTest extends AbstractWeldTest
{
   @Test
   public void testGameGenerator() throws Exception 
   {
     Game game1 = getReference(Game.class);
     Game game2 = getReference(Game.class);
     assert game1!=game2;
     assert game1.getNumber()!=game2.getNumber();
     Generator gen1 = getReference(Generator.class);
     Generator gen2 = getReference(Generator.class);
     assert gen1.getRandom()!=null;
     assert gen1.getRandom()==gen2.getRandom();
   }

   @Test
   public void testSentenceTranslator() throws Exception 
   {
      
      TextTranslator tt1 = getReference(TextTranslator.class);
      try 
      {
         tt1.translate("hello world");
         assert false;
      }
      catch (UnsupportedOperationException uoe)
      {
         //expected
      }
   }
   
}
