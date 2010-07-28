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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class NamedProducerTest extends AbstractWeldTest
{
   
   @Test
   public void testNamedProducer()
   {
      Bean<?> iemonBean = getCurrentManager().resolve(getCurrentManager().getBeans("iemon"));
      String[] iemon = (String[]) getCurrentManager().getReference(iemonBean, Object.class, getCurrentManager().createCreationalContext(iemonBean));
      assert iemon.length == 3;
      Bean<?> itoenBean = getCurrentManager().resolve(getCurrentManager().getBeans("itoen"));
      String[] itoen = (String[]) getCurrentManager().getReference(itoenBean, Object.class, getCurrentManager().createCreationalContext(itoenBean));
      assert itoen.length == 2;
   }
   
   @Test
   public void testDefaultNamedProducerMethod() 
   {
      Set<Bean<?>> beans = getCurrentManager().getBeans(JmsTemplate.class);
      assert beans.size() == 2;
      List<String> beanNames = new ArrayList<String>(Arrays.asList("errorQueueTemplate", "logQueueTemplate"));
      for (Bean<?> b : beans)
      {
         beanNames.remove(b.getName());
      }
      assert beanNames.isEmpty();
   }

}
