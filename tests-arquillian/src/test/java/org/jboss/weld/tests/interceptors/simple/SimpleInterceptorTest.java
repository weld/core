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

package org.jboss.weld.tests.interceptors.simple;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.resources.ClassTransformer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
@RunWith(Arquillian.class)
public class SimpleInterceptorTest 
{
   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .intercept(SimpleInterceptor.class, TwoBindingsInterceptor.class)
         .decorate(SimpleDecorator.class)
         .addPackage(SimpleInterceptorTest.class.getPackage());
   }

   @Inject
   private BeanManager beanManager;
   
   @Before
   public void resetInterceptors()
   {
      SimpleInterceptor.aroundInvokeCalled = false;
      SimpleInterceptor.postConstructCalled = false;
      SimpleInterceptor.preDestroyCalled = false;
      TwoBindingsInterceptor.aroundInvokeCalled = false;
      SimpleBeanImpl.postConstructCalled = false;
      SimpleBeanImpl.businessMethodInvoked = false;
   }


   @Test
   public void testInterceptorModel()
   {
      InterceptorBindingModel<SecondaryInterceptionBinding> interceptorBindingModel
            = new InterceptorBindingModel<SecondaryInterceptionBinding>(SecondaryInterceptionBinding.class, new ClassTransformer("STATIC_INSTANCE", new TypeStore()));
      Set<Annotation> annotations = interceptorBindingModel.getInheritedInterceptionBindingTypes();
      assert annotations.size() != 0;
   }

   @Test
   public void testSimpleInterceptor()
   {
      Bean bean = beanManager.getBeans(SimpleBeanImpl.class).iterator().next();
      CreationalContext creationalContext = beanManager.createCreationalContext(bean);
      SimpleBeanImpl simpleBean = (SimpleBeanImpl) bean.create(creationalContext);
      String result = simpleBean.doSomething();
      assert "decorated-Hello!-decorated".equals(result);
      bean.destroy(simpleBean, creationalContext);
      assert SimpleInterceptor.aroundInvokeCalled;
      assert !SimpleInterceptor.postConstructCalled;
      assert !SimpleInterceptor.preDestroyCalled;
      assert TwoBindingsInterceptor.aroundInvokeCalled;
      assert SimpleBeanImpl.postConstructCalled;
   }

   @Test
   public void testSimpleInterceptorWithStereotype()
   {
      Bean bean = beanManager.getBeans(SimpleBeanWithStereotype.class).iterator().next();
      CreationalContext creationalContext = beanManager.createCreationalContext(bean);
      SimpleBeanWithStereotype simpleBean = (SimpleBeanWithStereotype) bean.create(creationalContext);
      String result = simpleBean.doSomething();
      assert "Hello!".equals(result);
      bean.destroy(simpleBean, creationalContext);
      assert SimpleInterceptor.aroundInvokeCalled;
      assert SimpleInterceptor.postConstructCalled;
      assert SimpleInterceptor.preDestroyCalled;
      assert TwoBindingsInterceptor.aroundInvokeCalled;
      assert SimpleBeanWithStereotype.postConstructCalled;
   }
}
