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
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ScopeTest
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(ScopeTest.class.getPackage())
         .addClass(Utils.class);
   }
   
   private static Annotation USELESS_LITERAL = new AnnotationLiteral<Useless>() {};
   private static Annotation SPECIAL_LITERAL = new AnnotationLiteral<Special>() {};
   
   @Inject 
   private BeanManagerImpl beanManager;
   
   /*
    * description = "WELD-322"
    */
   @Test
   public void testScopeDeclaredOnSubclassOverridesScopeOnSuperClass()
   {
      Assert.assertEquals(Dependent.class, beanManager.resolve(beanManager.getBeans(Bar.class)).getScope());
   }
   
   /*
    * description = "WELD-311"
    */
   @Test
   public void testScopeOfProducerMethod()
   {
      Bean<Temp> specialTempBean = Utils.getBean(beanManager, Temp.class, SPECIAL_LITERAL);
      Bean<Temp> uselessTempBean = Utils.getBean(beanManager, Temp.class, USELESS_LITERAL);
      Assert.assertEquals(RequestScoped.class, specialTempBean.getScope());
      Assert.assertEquals(RequestScoped.class, uselessTempBean.getScope());
      Assert.assertEquals(10, Utils.getReference(beanManager, specialTempBean).getNumber());
      Assert.assertEquals(11, Utils.getReference(beanManager, uselessTempBean).getNumber());
      
      TempConsumer tempConsumer = Utils.getReference(beanManager, TempConsumer.class);
      tempConsumer.getSpecialTemp().setNumber(101);
      tempConsumer.getUselessTemp().setNumber(102);
      
      Assert.assertEquals(101, tempConsumer.getSpecialTemp().getNumber());
      Assert.assertEquals(102, tempConsumer.getUselessTemp().getNumber());
      Assert.assertEquals(101, Utils.getReference(beanManager, specialTempBean).getNumber());
      Assert.assertEquals(102, Utils.getReference(beanManager, uselessTempBean).getNumber());
      
      newRequest();
      
      Assert.assertEquals(10, tempConsumer.getSpecialTemp().getNumber());
      Assert.assertEquals(11, tempConsumer.getUselessTemp().getNumber());
      Assert.assertEquals(10, Utils.getReference(beanManager, specialTempBean).getNumber());
      Assert.assertEquals(11, Utils.getReference(beanManager, uselessTempBean).getNumber());
   }
   
   private void newRequest()
   {
      ContextLifecycle lifecycle = Container.instance().services().get(ContextLifecycle.class);
      lifecycle.endRequest("test", lifecycle.getRequestContext().getBeanStore());
      lifecycle.restoreSession("test", new HashMapBeanStore());
      lifecycle.beginRequest("test", new HashMapBeanStore());
   }

}
