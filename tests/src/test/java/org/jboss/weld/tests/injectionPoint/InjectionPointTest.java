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
package org.jboss.weld.tests.injectionPoint;

import java.lang.reflect.ParameterizedType;

import javax.enterprise.inject.IllegalProductException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.test.Utils;
import org.testng.annotations.Test;

@Artifact
public class InjectionPointTest extends AbstractWeldTest
{
   
   @Test(description="WELD-239")
   public void testCorrectInjectionPointUsed()
   {
      getReference(IntConsumer.class).ping();
      
      try
      {
         getReference(DoubleConsumer.class).ping();
      }
      catch (IllegalProductException e)
      {
         assert e.getMessage().contains("Injection Point: field org.jboss.weld.tests.injectionPoint.DoubleGenerator.timer");
      }
   }
   
   @Test(description="WELD-316")
   public void testFieldInjectionPointSerializability() throws Throwable
   {
      getReference(StringConsumer.class).ping();
      InjectionPoint ip = StringGenerator.getInjectionPoint();
      assert ip != null;
      assert ip.getMember().getName().equals("str");
      InjectionPoint ip1 = Utils.deserialize(Utils.serialize(ip));
      assert ip1.getMember().getName().equals("str");
   }
   
   @Test
   public void testGetDeclaringType()
   {
      assert getReference(GrassyField.class).getCow().getName().equals("daisy");
   }
   
   @Test(description = "WELD-438")
   public void testInjectionPointWhenInstanceGetIsUsed() throws Exception
   {
      Pig pig = getReference(PigSty.class).getPig();
      assert pig != null;
      assert pig.getInjectionPoint().getBean() != null;
      assert pig.getInjectionPoint().getBean().getBeanClass().equals(PigSty.class);
      assert pig.getInjectionPoint().getMember().equals(PigSty.class.getDeclaredField("pig"));
      assert pig.getInjectionPoint().getAnnotated() != null;
      assert pig.getInjectionPoint().getAnnotated().getBaseType() instanceof ParameterizedType;
      ParameterizedType parameterizedType = ((ParameterizedType) pig.getInjectionPoint().getAnnotated().getBaseType());
      assert parameterizedType.getRawType().equals(Instance.class);
      assert parameterizedType.getActualTypeArguments().length == 1;
      assert parameterizedType.getActualTypeArguments()[0].equals(Pig.class);
      assert pig.getInjectionPoint().getAnnotated().isAnnotationPresent(Special.class);
      assert !pig.getInjectionPoint().getAnnotated().isAnnotationPresent(ExtraSpecial.class);
      assert Utils.annotationSetMatches(pig.injectionPoint.getQualifiers(), Special.class, ExtraSpecial.class);
      assert Pig.class.equals(pig.getInjectionPoint().getType());
      
   }

}
