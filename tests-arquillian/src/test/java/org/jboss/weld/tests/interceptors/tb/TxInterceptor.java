/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.tb;

import java.io.Serializable;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Tx
@Interceptor
public class TxInterceptor implements Serializable {
    static boolean ignoreDup = false;
    static ThreadLocal<Client> clients = new ThreadLocal<Client>();

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext invocation) throws Exception {
        System.err.println("invocation = " + invocation);

        if (ignoreDup == false && clients.get() != null)
            throw new IllegalArgumentException("Didn't expect duplicate call!");

        Client client = new Client();
        client.name = "TxInterceptor_TEMP";
        clients.set(client);
        try {
            return invocation.proceed();
        } finally {
            clients.remove();
        }
    }
}
