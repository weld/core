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
package org.jboss.weld.tests.proxy;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
class Foo implements Serializable, Bar {
    public static final String MESSAGE = "Hi";

    public String getRealMsg(int param1, long param2, double param3, boolean param4, char param5, float param7, short param8) {
        return MESSAGE;
    }

    // test all primitive parameter types to make sure the bytecode is generated
    // correctly for each of them
    public String getMsg(int param1, long param2, double param3, boolean param4, char param5, float param7, short param8) {
        return MESSAGE;
    }

}
