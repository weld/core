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

package org.jboss.weld.tests.interceptors.ejb3model;

import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;

/**
 * @author Marius Bogoevici
 */
@Interceptors({ Goalkeeper.class, Referee.class })
@Dependent
public class Ball {
    public static boolean played = false;

    public static boolean aroundInvoke = false;

    @ExcludeClassInterceptors
    @Interceptors(Defender.class)
    public void shoot() {
        played = true;
    }

    @Interceptors(Defender.class)
    public void pass() {
        played = true;
    }

    public void lob() {
        played = true;
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        aroundInvoke = true;
        return invocationContext.proceed();
    }
}
