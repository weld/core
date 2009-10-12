/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.test.unit.interceptor.passivation;

import java.io.Serializable;

import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;

/**
 * @author Marius Bogoevici
 */
@Interceptor @PassivationActivation
public class PassivationActivationInterceptor implements Serializable
{
   public static boolean prePassivateInvoked;
   public static boolean postActivateInvoked;

   @PrePassivate
   public void prePassivate(InvocationContext invocationContext) throws Exception
   {
      prePassivateInvoked = true;
      invocationContext.proceed();
   }


   @PostActivate
   public void postActivate(InvocationContext invocationContext) throws Exception
   {
      postActivateInvoked = true;
      invocationContext.proceed();
   }

}
