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
package org.jboss.weld.tests.enterprise;

import java.io.IOException;

import jakarta.inject.Inject;

import org.jboss.weld.tests.util.BeanPassivator;

public class HelloAction {

    @Inject
    private IHelloBean helloBean;

    private String hello;
    private String goodBye;

    public void executeRequest() {
        hello = helloBean.sayHello();
        try {
            byte[] passivated = BeanPassivator.passivate(helloBean);
            Object activated = BeanPassivator.activate(passivated);
            goodBye = ((IHelloBean) activated).sayGoodbye();

            // following exception are rethrown as they basically mean test failure!
        } catch (IOException ex) {
            throw new IllegalStateException("There was a problem with passivation!" + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("There was a problem with activation of passivated bean!" + ex.getMessage());
        }
    }

    public String getHello() {
        return hello;
    }

    public String getGoodBye() {
        return goodBye;
    }

}
