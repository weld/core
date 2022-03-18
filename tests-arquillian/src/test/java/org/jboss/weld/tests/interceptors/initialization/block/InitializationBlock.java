/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.tests.interceptors.initialization.block;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.context.Dependent;
import jakarta.transaction.UserTransaction;

/**
 *@author Emily Jiang
 */
@Dependent
public class InitializationBlock{

    public final String VALUE;

    protected UserTransaction ut;

    {
        try {
            ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public InitializationBlock() {
        VALUE = "initialized!";
    }

    @Dummy
    public Integer dummyMethod() {
        return 5;
    }
}
