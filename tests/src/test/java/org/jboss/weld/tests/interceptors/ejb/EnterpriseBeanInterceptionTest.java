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
package org.jboss.weld.tests.interceptors.ejb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Timer;
import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.ejb.spi.InterceptorBindings;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
@BeansXml("beans.xml")
public class EnterpriseBeanInterceptionTest extends AbstractWeldTest
{

   @Test(groups = { "interceptors", "incontainer-broken"})
   public void testInterceptors() throws Exception
   {
      SessionBean<Ball> ballSessionBean = (SessionBean<Ball>)getCurrentManager().getBeans(Ball.class).iterator().next();
      InterceptorBindings interceptorBindings = new InterceptorBindingsAdapter(getCurrentManager().getCdiInterceptorsRegistry().getInterceptionModel(ballSessionBean.getType()));
      List<javax.enterprise.inject.spi.Interceptor> interceptors =
            new ArrayList<javax.enterprise.inject.spi.Interceptor>(interceptorBindings.getAllInterceptors());

      assert interceptors.size() == 3;
      List<Class<?>> expectedInterceptors = Arrays.<Class<?>>asList(Goalkeeper.class, Defender.class, Referee.class);
      assert expectedInterceptors.contains(interceptors.get(0).getBeanClass());
      assert expectedInterceptors.contains(interceptors.get(1).getBeanClass());
      assert expectedInterceptors.contains(interceptors.get(2).getBeanClass());


      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_TIMEOUT, ballSessionBean.getType().getMethod("shoot")).size() == 0;
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_INVOKE, ballSessionBean.getType().getMethod("shoot")).size() == 1;
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_INVOKE, ballSessionBean.getType().getMethod("shoot")).get(0).getBeanClass().equals(Goalkeeper.class);
      
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_TIMEOUT, ballSessionBean.getType().getMethod("pass")).size() == 0;
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_INVOKE, ballSessionBean.getType().getMethod("pass")).size() == 1;
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_INVOKE, ballSessionBean.getType().getMethod("pass")).get(0).getBeanClass().equals(Defender.class);

      Method finishGameMethod = ballSessionBean.getType().getMethod("finishGame", Timer.class);
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_INVOKE, finishGameMethod).size() == 0;
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_TIMEOUT, finishGameMethod).size() == 1;
      assert interceptorBindings.getMethodInterceptors(InterceptionType.AROUND_TIMEOUT, finishGameMethod).get(0).getBeanClass().equals(Referee.class);

   }

}
