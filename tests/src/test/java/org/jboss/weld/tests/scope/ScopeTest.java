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
package org.jboss.weld.tests.scope;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ScopeTest extends AbstractWeldTest
{
   
   private static Annotation USELESS_LITERAL = new AnnotationLiteral<Useless>() {};
   private static Annotation SPECIAL_LITERAL = new AnnotationLiteral<Special>() {};
   
   @Test(description="WELD-322")
   public void testScopeDeclaredOnSubclassOverridesScopeOnSuperClass()
   {
      assert getCurrentManager().resolve(getCurrentManager().getBeans(Bar.class)).getScope().equals(Dependent.class);
   }
   @Test(description="WELD-311")
   public void testScopeOfProducerMethod()
   {
      Bean<Temp> specialTempBean = getBean(Temp.class, SPECIAL_LITERAL);
      Bean<Temp> uselessTempBean = getBean(Temp.class, USELESS_LITERAL);
      assert specialTempBean.getScope().equals(RequestScoped.class);
      assert uselessTempBean.getScope().equals(RequestScoped.class);
      assert getReference(specialTempBean).getNumber() == 10;
      assert getReference(uselessTempBean).getNumber() == 11;
      
      TempConsumer tempConsumer = getReference(TempConsumer.class);
      tempConsumer.getSpecialTemp().setNumber(101);
      tempConsumer.getUselessTemp().setNumber(102);
      
      assert tempConsumer.getSpecialTemp().getNumber() == 101;
      assert tempConsumer.getUselessTemp().getNumber() == 102;
      assert getReference(specialTempBean).getNumber() == 101;
      assert getReference(uselessTempBean).getNumber() == 102;
      
      newRequest();
      
      assert tempConsumer.getSpecialTemp().getNumber() == 10;
      assert tempConsumer.getUselessTemp().getNumber() == 11;
      assert getReference(specialTempBean).getNumber() == 10;
      assert getReference(uselessTempBean).getNumber() == 11;
   }
   
   private void newRequest()
   {
      ContextLifecycle lifecycle = Container.instance().services().get(ContextLifecycle.class);
      lifecycle.endRequest("test", lifecycle.getRequestContext().getBeanStore());
      lifecycle.restoreSession("test", new HashMapBeanStore());
      lifecycle.beginRequest("test", new HashMapBeanStore());
   }

}
